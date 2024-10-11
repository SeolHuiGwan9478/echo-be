package woozlabs.echo.domain.sharedEmail.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.gmail.service.GmailService;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.repository.AccountRepository;
import woozlabs.echo.domain.sharedEmail.dto.*;
import woozlabs.echo.domain.sharedEmail.entity.Permission;
import woozlabs.echo.domain.sharedEmail.entity.SharedDataType;
import woozlabs.echo.domain.sharedEmail.entity.SharedEmail;
import woozlabs.echo.domain.sharedEmail.repository.SharedInboxRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SharedInboxService {

    private final SharedInboxRepository sharedInboxRepository;
    private final AccountRepository accountRepository;
    private final InviteShareEmailService inviteShareEmailService;
    private final GmailService gmailService;

    private static String generateId(String id, SharedDataType sharedDataType) {
        if (sharedDataType.equals(SharedDataType.THREAD)) {
            return "t_" + id;
        } else if (sharedDataType.equals(SharedDataType.MESSAGE)) {
            return "m_" + id;
        }
        return id;
    }

    @Transactional
    public SharedEmailResponseDto createSharePost(String uid, CreateSharedRequestDto createSharedRequestDto) {
        Account account = accountRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));

        String generatedDataId = generateId(createSharedRequestDto.getDataId(), createSharedRequestDto.getSharedDataType());

        Optional<SharedEmail> existingSharedEmail = sharedInboxRepository.findByDataId(generatedDataId);
        if (existingSharedEmail.isPresent()) {
            log.info("SharedEmail already exists for dataId: {}", generatedDataId);
            
            SharedEmail sharedEmail = existingSharedEmail.get();

            // 이미 존재하는 경우, 기존 데이터를 반환 (덮어씌우는 대신 반환만)
            return SharedEmailResponseDto.builder()
                    .id(sharedEmail.getId())
                    .access(sharedEmail.getAccess())
                    .dataId(sharedEmail.getDataId())
                    .sharedDataType(sharedEmail.getSharedDataType())
                    .canEditorEditPermission(sharedEmail.isCanEditorEditPermission())
                    .canViewerViewToolMenu(sharedEmail.isCanViewerViewToolMenu())
                    .inviteePermissions(sharedEmail.getInviteePermissions())
                    .createdAt(sharedEmail.getCreatedAt())
                    .updatedAt(sharedEmail.getUpdatedAt())
                    .build();
        }

        Map<String, Permission> inviteePermissions = new HashMap<>();
        inviteePermissions.put(account.getEmail(), Permission.OWNER);

        SharedEmail sharedEmail = SharedEmail.builder()
                .access(createSharedRequestDto.getAccess())
                .dataId(generatedDataId)
                .sharedDataType(createSharedRequestDto.getSharedDataType())
                .owner(account)
                .canEditorEditPermission(createSharedRequestDto.isCanEditorEditPermission())
                .canViewerViewToolMenu(createSharedRequestDto.isCanViewerViewToolMenu())
                .inviteePermissions(inviteePermissions)
                .build();

        sharedEmail = sharedInboxRepository.save(sharedEmail);
        log.info("New SharedEmail saved with id: {}", sharedEmail.getId());

        // 초대 권한 생성 및 저장
        return SharedEmailResponseDto.builder()
                .id(sharedEmail.getId())
                .access(sharedEmail.getAccess())
                .dataId(sharedEmail.getDataId())
                .sharedDataType(sharedEmail.getSharedDataType())
                .canEditorEditPermission(sharedEmail.isCanEditorEditPermission())
                .canViewerViewToolMenu(sharedEmail.isCanViewerViewToolMenu())
                .inviteePermissions(inviteePermissions)
                .createdAt(sharedEmail.getCreatedAt())
                .updatedAt(sharedEmail.getUpdatedAt())
                .build();
    }

    @Transactional
    public SharedEmailResponseDto inviteToSharedPost(String uid, UUID sharedEmailId, SendSharedEmailInvitationDto sendSharedEmailInvitationDto) {
        Account account = accountRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));

        SharedEmail sharedEmail = sharedInboxRepository.findById(sharedEmailId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SHARED_EMAIL));

        if (!sharedEmail.getInviteePermissions().getOrDefault(account.getEmail(), Permission.VIEWER).equals(Permission.OWNER)) {
            throw new CustomErrorException(ErrorCode.FORBIDDEN_ACCESS_TO_SHARED_EMAIL);
        }

        List<String> invitees = sendSharedEmailInvitationDto.getInvitees();
        Map<String, Permission> currentPermissions = new HashMap<>(sharedEmail.getInviteePermissions());

        for (String invitee : invitees) {
            if (invitee.equals(account.getEmail())) {
                continue;
            }

            currentPermissions.put(invitee, sendSharedEmailInvitationDto.getPermission());

            if (sendSharedEmailInvitationDto.isNotifyInvitation()) {
                accountRepository.findByEmail(invitee).ifPresentOrElse(
                        existingAccount -> {
                            inviteShareEmailService.sendEmailViaSES(existingAccount.getEmail(), sendSharedEmailInvitationDto.getInvitationMemo(), sendSharedEmailInvitationDto);
                        },
                        () -> {
                            inviteShareEmailService.sendEmailViaSES(invitee, "This email grants access to this item without logging in. Only forward it to people you trust.\n" + sendSharedEmailInvitationDto.getInvitationMemo(), sendSharedEmailInvitationDto);
                        }
                );
            }
        }

        sharedEmail.setInviteePermissions(currentPermissions);
        sharedEmail = sharedInboxRepository.save(sharedEmail);

        return SharedEmailResponseDto.builder()
                .id(sharedEmail.getId())
                .access(sharedEmail.getAccess())
                .dataId(sharedEmail.getDataId())
                .sharedDataType(sharedEmail.getSharedDataType())
                .canEditorEditPermission(sharedEmail.isCanEditorEditPermission())
                .canViewerViewToolMenu(sharedEmail.isCanViewerViewToolMenu())
                .inviteePermissions(sharedEmail.getInviteePermissions())
                .createdAt(sharedEmail.getCreatedAt())
                .updatedAt(sharedEmail.getUpdatedAt())
                .build();
    }

    @Transactional
    public SharedEmailResponseDto updateSharedPost(String uid, UUID sharedEmailId, UpdateSharedPostDto updateDto) {
        Account account = accountRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));

        SharedEmail sharedEmail = sharedInboxRepository.findById(sharedEmailId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SHARED_EMAIL));

        if (!sharedEmail.getInviteePermissions().getOrDefault(account.getEmail(), Permission.VIEWER).equals(Permission.OWNER)) {
            throw new CustomErrorException(ErrorCode.FORBIDDEN_ACCESS_TO_SHARED_EMAIL);
        }

        if (updateDto.getAccess() != null) {
            sharedEmail.setAccess(updateDto.getAccess());
        }
        if (updateDto.getCanEditorEditPermission() != null) {
            sharedEmail.setCanEditorEditPermission(updateDto.getCanEditorEditPermission());
        }
        if (updateDto.getCanViewerViewToolMenu() != null) {
            sharedEmail.setCanViewerViewToolMenu(updateDto.getCanViewerViewToolMenu());
        }

        sharedInboxRepository.save(sharedEmail);

        return SharedEmailResponseDto.builder()
                .id(sharedEmail.getId())
                .access(sharedEmail.getAccess())
                .dataId(sharedEmail.getDataId())
                .sharedDataType(sharedEmail.getSharedDataType())
                .canEditorEditPermission(sharedEmail.isCanEditorEditPermission())
                .canViewerViewToolMenu(sharedEmail.isCanViewerViewToolMenu())
                .inviteePermissions(sharedEmail.getInviteePermissions())
                .createdAt(sharedEmail.getCreatedAt())
                .updatedAt(sharedEmail.getUpdatedAt())
                .build();
    }

    public GetSharedEmailResponseDto getSharedEmail(String uid, UUID sharedEmailId) {
        log.info("Fetching shared email for UID: {}", uid);

        SharedEmail sharedEmail = sharedInboxRepository.findById(sharedEmailId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SHARED_EMAIL));

        Permission permissionLevel = Permission.VIEWER;
        Account account = null;

        if (uid != null) {
            account = accountRepository.findByUid(uid)
                    .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));

            String userEmail = account.getEmail();
            if (sharedEmail.getInviteePermissions().containsKey(userEmail)) {
                permissionLevel = sharedEmail.getInviteePermissions().get(userEmail);
                log.debug("User {} has permission level {} for SharedEmail {}", userEmail, permissionLevel, sharedEmailId);
            } else {
                log.error("Forbidden access to shared email for account: {}", userEmail);
                throw new CustomErrorException(ErrorCode.FORBIDDEN_ACCESS_TO_SHARED_EMAIL);
            }
        } else {
            permissionLevel = Permission.PUBLIC_VIEWER;
            log.debug("Anonymous access to SharedEmail {}. Setting permission to PUBLIC_VIEWER", sharedEmailId);
        }

        String dataId = sharedEmail.getDataId();
        if (dataId.startsWith("m_") || dataId.startsWith("t_")) {
            dataId = dataId.substring(2);
        }

        Object sharedEmailData;
        try {
            String ownerUid = sharedEmail.getOwner().getUid();
            Account ownerAccount = accountRepository.findByUid(ownerUid)
                    .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));

            String ownerAccessToken = ownerAccount.getAccessToken();
            if (sharedEmail.getSharedDataType() == SharedDataType.THREAD) {
                log.debug("Fetching thread data for SharedEmail {}. Owner UID: {}", sharedEmailId, ownerUid);
                sharedEmailData = gmailService.getUserEmailThread(ownerAccessToken, dataId);
            } else if (sharedEmail.getSharedDataType() == SharedDataType.MESSAGE) {
                log.debug("Fetching message data for SharedEmail {}. Owner UID: {}", sharedEmailId, ownerUid);
                sharedEmailData = gmailService.getUserEmailMessage(ownerAccessToken, dataId);
            } else {
                throw new CustomErrorException(ErrorCode.INVALID_SHARED_DATA_TYPE);
            }
        } catch (CustomErrorException e) {
            if (e.getErrorCode() == ErrorCode.NOT_FOUND_GMAIL_THREAD) {
                sharedInboxRepository.delete(sharedEmail);
                log.warn("Shared email deleted for dataId: {} due to missing Gmail thread", sharedEmail.getDataId());
                throw new CustomErrorException(ErrorCode.EMAIL_DATA_NOT_FOUND_AND_REMOVED, e.getMessage());
            }
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Map<String, Permission> inviteePermissions = new HashMap<>(sharedEmail.getInviteePermissions());
        inviteePermissions.replaceAll((email, permission) -> {
            boolean isPresent = accountRepository.findByEmail(email).isPresent();
            return isPresent ? permission : Permission.PUBLIC_VIEWER;
        });

        // 권한에 따라 반환할 inviteePermission 설정
        Map<String, Permission> filteredPermissions = new HashMap<>();
        String ownerEmail = sharedEmail.getOwner().getEmail();

        if (permissionLevel == Permission.OWNER || permissionLevel == Permission.EDITOR) {
            filteredPermissions = inviteePermissions;
        } else if (permissionLevel == Permission.VIEWER) {
            filteredPermissions.put(ownerEmail, Permission.OWNER);
            if (account != null) {
                filteredPermissions.put(account.getEmail(), Permission.VIEWER);
            }
        } else if (permissionLevel == Permission.PUBLIC_VIEWER) {
            filteredPermissions.put(ownerEmail, Permission.OWNER);
        }

        log.info("Successfully fetched shared email data for SharedEmailId: {}", sharedEmailId);
        return GetSharedEmailResponseDto.builder()
                .sharedEmailData(sharedEmailData)
                .dataId(sharedEmail.getDataId())
                .permissionLevel(permissionLevel)
                .canEdit(permissionLevel == Permission.EDITOR && sharedEmail.isCanEditorEditPermission())
                .canViewToolMenu(permissionLevel == Permission.EDITOR || (permissionLevel == Permission.OWNER && sharedEmail.isCanViewerViewToolMenu()))
                .sharedDataType(sharedEmail.getSharedDataType())
                .inviteePermissions(filteredPermissions)
                .build();
    }


    @Transactional
    public UpdateInviteePermissionsDto updateInviteePermissions(String uid, UUID sharedEmailId, UpdateInviteePermissionsDto updateInviteePermissionsDto) {
        Account account = accountRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));

        SharedEmail sharedEmail = sharedInboxRepository.findById(sharedEmailId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SHARED_EMAIL));

        Permission accountPermission = sharedEmail.getInviteePermissions().getOrDefault(account.getEmail(), Permission.VIEWER);

        if (!(accountPermission.equals(Permission.OWNER) || accountPermission.equals(Permission.EDITOR))) {
            throw new CustomErrorException(ErrorCode.FORBIDDEN_ACCESS_TO_SHARED_EMAIL);
        }

        Map<String, Permission> currentPermissions = new HashMap<>(sharedEmail.getInviteePermissions());
        for (Map.Entry<String, Permission> entry : updateInviteePermissionsDto.getInviteePermissions().entrySet()) {
            String email = entry.getKey();
            Permission newPermission = entry.getValue();

            if (currentPermissions.containsKey(email)) {
                currentPermissions.put(email, newPermission);
            } else {
                throw new CustomErrorException(ErrorCode.INVITEE_NOT_FOUND_ERROR);
            }
        }

        sharedEmail.setInviteePermissions(currentPermissions);
        sharedInboxRepository.save(sharedEmail);

        UpdateInviteePermissionsDto updatedPermissionsDto = new UpdateInviteePermissionsDto();
        updatedPermissionsDto.setInviteePermissions(currentPermissions);

        return updatedPermissionsDto;
    }

    @Transactional
    public SharedEmailResponseDto excludeInvitees(String uid, UUID sharedEmailId, ExcludeInviteesRequestDto excludeInviteesRequestDto) {
        Account account = accountRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));

        SharedEmail sharedEmail = sharedInboxRepository.findById(sharedEmailId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SHARED_EMAIL));

        Permission accountPermission = sharedEmail.getInviteePermissions().getOrDefault(account.getEmail(), Permission.VIEWER);

        if (!(accountPermission.equals(Permission.OWNER) || accountPermission.equals(Permission.EDITOR))) {
            throw new CustomErrorException(ErrorCode.FORBIDDEN_ACCESS_TO_SHARED_EMAIL);
        }

        Map<String, Permission> currentPermissions = sharedEmail.getInviteePermissions();
        List<String> inviteeEmails = excludeInviteesRequestDto.getInviteeEmails();

        for (String invitee : inviteeEmails) {
            if (currentPermissions.containsKey(invitee)) {
                currentPermissions.remove(invitee);
            } else {
                throw new CustomErrorException(ErrorCode.INVITEE_NOT_FOUND_ERROR);
            }
        }

        sharedEmail.setInviteePermissions(currentPermissions);
        sharedInboxRepository.save(sharedEmail);

        return SharedEmailResponseDto.builder()
                .id(sharedEmail.getId())
                .access(sharedEmail.getAccess())
                .dataId(sharedEmail.getDataId())
                .sharedDataType(sharedEmail.getSharedDataType())
                .canEditorEditPermission(sharedEmail.isCanEditorEditPermission())
                .canViewerViewToolMenu(sharedEmail.isCanViewerViewToolMenu())
                .inviteePermissions(currentPermissions)
                .createdAt(sharedEmail.getCreatedAt())
                .updatedAt(sharedEmail.getUpdatedAt())
                .build();
    }
}
