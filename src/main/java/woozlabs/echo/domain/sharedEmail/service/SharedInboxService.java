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
import woozlabs.echo.domain.sharedEmail.entity.SharedEmailPermission;
import woozlabs.echo.domain.sharedEmail.repository.SharedEmailPermissionRepository;
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
    private final SharedEmailPermissionRepository sharedEmailPermissionRepository;
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

        Optional<SharedEmail> existingSharedEmail = sharedInboxRepository.findByDataId(createSharedRequestDto.getDataId());
        if (existingSharedEmail.isPresent()) {
            log.info("SharedEmail already exists for dataId: {}", createSharedRequestDto.getDataId());
            SharedEmail sharedEmail = existingSharedEmail.get();

            // 이미 존재하는 경우, 기존 데이터를 반환 (덮어씌우는 대신 반환만)
            return SharedEmailResponseDto.builder()
                    .id(sharedEmail.getId())
                    .access(sharedEmail.getAccess())
                    .dataId(sharedEmail.getDataId())
                    .sharedDataType(sharedEmail.getSharedDataType())
                    .canEditorEditPermission(sharedEmail.isCanEditorEditPermission())
                    .canViewerViewToolMenu(sharedEmail.isCanViewerViewToolMenu())
                    .inviteePermissions(sharedEmail.getSharedEmailPermission().getInviteePermissions()) // 초대된 권한 포함
                    .createdAt(sharedEmail.getCreatedAt())
                    .updatedAt(sharedEmail.getUpdatedAt())
                    .build();
        }

        // 새로운 SharedEmail 생성
        log.info("Creating new SharedEmail for dataId: {}", generatedDataId);
        SharedEmail sharedEmail = SharedEmail.builder()
                .access(createSharedRequestDto.getAccess())
                .dataId(generatedDataId)
                .sharedDataType(createSharedRequestDto.getSharedDataType())
                .owner(account)
                .canEditorEditPermission(createSharedRequestDto.isCanEditorEditPermission())
                .canViewerViewToolMenu(createSharedRequestDto.isCanViewerViewToolMenu())
                .build();

        sharedInboxRepository.save(sharedEmail);
        log.info("New SharedEmail saved with id: {}", sharedEmail.getId());

        // 초대 권한 생성 및 저장
        Map<String, Permission> inviteePermissions = new HashMap<>();
        inviteePermissions.put(account.getEmail(), Permission.OWNER);

        log.info("Setting owner permission for account email: {}", account.getEmail());
        SharedEmailPermission sharedEmailPermission = SharedEmailPermission.builder()
                .sharedEmail(sharedEmail)
                .inviteePermissions(inviteePermissions)
                .build();

        sharedEmailPermissionRepository.save(sharedEmailPermission);
        log.info("SharedEmailPermission saved for sharedEmailId: {}", sharedEmail.getId());

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

        SharedEmailPermission sharedEmailPermission = sharedEmailPermissionRepository.findBySharedEmailId(sharedEmailId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SHARED_EMAIL_PERMISSION));

        if (!sharedEmailPermission.getInviteePermissions().getOrDefault(account.getEmail(), Permission.VIEWER).equals(Permission.OWNER)) {
            throw new CustomErrorException(ErrorCode.FORBIDDEN_ACCESS_TO_SHARED_EMAIL);
        }

        List<String> invitees = sendSharedEmailInvitationDto.getInvitees();
        Map<String, Permission> newInviteePermissions = new HashMap<>();

        if (sendSharedEmailInvitationDto.isNotifyInvitation()) {
            for (String invitee : invitees) {
                if (invitee.equals(account.getEmail())) {
                    continue;
                }

                accountRepository.findByEmail(invitee).ifPresentOrElse(
                        existingAccount -> {
                            newInviteePermissions.put(invitee, sendSharedEmailInvitationDto.getPermission());
                            inviteShareEmailService.sendEmailViaSES(existingAccount.getEmail(), sendSharedEmailInvitationDto.getInvitationMemo(), sendSharedEmailInvitationDto);
                        },
                        () -> {
                            newInviteePermissions.put(invitee, sendSharedEmailInvitationDto.getPermission());
                            inviteShareEmailService.sendEmailViaSES(invitee, "This email grants access to this item without logging in. Only forward it to people you trust.\n" + sendSharedEmailInvitationDto.getInvitationMemo(), sendSharedEmailInvitationDto);
                        }
                );
            }
        } else {
            for (String invitee : invitees) {
                newInviteePermissions.put(invitee, sendSharedEmailInvitationDto.getPermission());
            }
        }

        Map<String, Permission> currentPermissions = sharedEmailPermission.getInviteePermissions();
        currentPermissions.putAll(newInviteePermissions);

        sharedEmailPermission.setInviteePermissions(currentPermissions);
        sharedEmailPermissionRepository.save(sharedEmailPermission);

        return SharedEmailResponseDto.builder()
                .id(sharedEmail.getId())
                .access(sharedEmail.getAccess())
                .dataId(sharedEmail.getDataId())
                .sharedDataType(sharedEmail.getSharedDataType())
                .canEditorEditPermission(sharedEmail.isCanEditorEditPermission())
                .canViewerViewToolMenu(sharedEmail.isCanViewerViewToolMenu())
                .inviteePermissions(sharedEmailPermission.getInviteePermissions())
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

        SharedEmailPermission sharedEmailPermission = sharedEmailPermissionRepository.findBySharedEmailId(sharedEmailId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SHARED_EMAIL_PERMISSION));

        if (!sharedEmailPermission.getInviteePermissions().getOrDefault(account.getEmail(), Permission.VIEWER).equals(Permission.OWNER)) {
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
                .inviteePermissions(sharedEmailPermission.getInviteePermissions())
                .createdAt(sharedEmail.getCreatedAt())
                .updatedAt(sharedEmail.getUpdatedAt())
                .build();
    }

    public GetSharedEmailResponseDto getSharedEmail(String uid, UUID sharedEmailId) {
        log.info("Fetching shared email for UID: {}", uid);

        SharedEmail sharedEmail = sharedInboxRepository.findById(sharedEmailId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SHARED_EMAIL));

        SharedEmailPermission sharedEmailPermission = sharedEmailPermissionRepository.findBySharedEmailId(sharedEmailId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SHARED_EMAIL_PERMISSION));

        Permission permissionLevel = Permission.VIEWER;
        Account account = null;

        if (uid != null) {
            account = accountRepository.findByUid(uid)
                    .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));

            String userEmail = account.getEmail();
            if (sharedEmailPermission.getInviteePermissions().containsKey(userEmail)) {
                permissionLevel = sharedEmailPermission.getInviteePermissions().get(userEmail);
            } else {
                log.error("Forbidden access to shared email for account: {}", userEmail);
                throw new CustomErrorException(ErrorCode.FORBIDDEN_ACCESS_TO_SHARED_EMAIL);
            }
        } else {
            permissionLevel = Permission.PUBLIC_VIEWER;
        }

        String dataId = sharedEmailPermission.getSharedEmail().getDataId();
        if (dataId.startsWith("m_") || dataId.startsWith("t_")) {
            dataId = dataId.substring(2);
        }

        Object sharedEmailData;
        try {
            String ownerUid = sharedEmailPermission.getSharedEmail().getOwner().getUid();
            if (sharedEmail.getSharedDataType() == SharedDataType.THREAD) {
                sharedEmailData = gmailService.getUserEmailThread(ownerUid, sharedEmailPermission.getSharedEmail().getDataId());
            } else if (sharedEmail.getSharedDataType() == SharedDataType.MESSAGE) {
                sharedEmailData = gmailService.getUserEmailMessage(ownerUid, sharedEmailPermission.getSharedEmail().getDataId());
            } else {
                throw new CustomErrorException(ErrorCode.INVALID_SHARED_DATA_TYPE);
            }
        } catch (CustomErrorException e) {
            if (e.getErrorCode() == ErrorCode.NOT_FOUND_GMAIL_THREAD) {
                sharedInboxRepository.delete(sharedEmailPermission.getSharedEmail());
                log.warn("Shared email deleted for dataId: {} due to missing Gmail thread", sharedEmailPermission.getSharedEmail().getDataId());
                throw new CustomErrorException(ErrorCode.EMAIL_DATA_NOT_FOUND_AND_REMOVED, e.getMessage());
            }
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Map<String, Permission> inviteePermissions = new HashMap<>(sharedEmailPermission.getInviteePermissions());
        inviteePermissions.replaceAll((email, permission) ->
            accountRepository.findByEmail(email).isPresent() ? permission : Permission.PUBLIC_VIEWER);

        // 권한에 따라 반환할 inviteePermission 설정
        Map<String, Permission> filteredPermissions = new HashMap<>();
        String ownerEmail = sharedEmailPermission.getSharedEmail().getOwner().getEmail();

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

        return GetSharedEmailResponseDto.builder()
                .sharedEmailData(sharedEmailData)
                .dataId(sharedEmailPermission.getSharedEmail().getDataId())
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

        SharedEmailPermission sharedEmailPermission = sharedEmailPermissionRepository.findBySharedEmailId(sharedEmailId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SHARED_EMAIL_PERMISSION));

        Permission accountPermission = sharedEmailPermission.getInviteePermissions().getOrDefault(account.getEmail(), Permission.VIEWER);

        if (!(accountPermission.equals(Permission.OWNER) || accountPermission.equals(Permission.EDITOR))) {
            throw new CustomErrorException(ErrorCode.FORBIDDEN_ACCESS_TO_SHARED_EMAIL);
        }

        Map<String, Permission> currentPermissions = sharedEmailPermission.getInviteePermissions();
        for (Map.Entry<String, Permission> entry : updateInviteePermissionsDto.getInviteePermissions().entrySet()) {
            String email = entry.getKey();
            Permission newPermission = entry.getValue();

            if (currentPermissions.containsKey(email)) {
                currentPermissions.put(email, newPermission);
            } else {
                throw new CustomErrorException(ErrorCode.INVITEE_NOT_FOUND_ERROR);
            }
        }

        sharedEmailPermission.setInviteePermissions(currentPermissions);
        sharedEmailPermissionRepository.save(sharedEmailPermission);

        UpdateInviteePermissionsDto updatedPermissionsDto = new UpdateInviteePermissionsDto();
        updatedPermissionsDto.setInviteePermissions(currentPermissions);

        return updatedPermissionsDto;
    }

    @Transactional
    public SharedEmailResponseDto excludeInvitees(String uid, UUID sharedEmailId, ExcludeInviteesRequestDto excludeInviteesRequestDto) {
        Account account = accountRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));

        SharedEmailPermission sharedEmailPermission = sharedEmailPermissionRepository.findBySharedEmailId(sharedEmailId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SHARED_EMAIL_PERMISSION));

        Permission accountPermission = sharedEmailPermission.getInviteePermissions().getOrDefault(account.getEmail(), Permission.VIEWER);

        if (!(accountPermission.equals(Permission.OWNER) || accountPermission.equals(Permission.EDITOR))) {
            throw new CustomErrorException(ErrorCode.FORBIDDEN_ACCESS_TO_SHARED_EMAIL);
        }

        Map<String, Permission> currentPermissions = sharedEmailPermission.getInviteePermissions();
        List<String> inviteeEmails = excludeInviteesRequestDto.getInviteeEmails();

        for (String invitee : inviteeEmails) {
            if (currentPermissions.containsKey(invitee)) {
                currentPermissions.remove(invitee);
            } else {
                throw new CustomErrorException(ErrorCode.INVITEE_NOT_FOUND_ERROR);
            }
        }

        sharedEmailPermission.setInviteePermissions(currentPermissions);
        sharedEmailPermissionRepository.save(sharedEmailPermission);

        return SharedEmailResponseDto.builder()
                .id(sharedEmailPermission.getSharedEmail().getId())
                .access(sharedEmailPermission.getSharedEmail().getAccess())
                .dataId(sharedEmailPermission.getSharedEmail().getDataId())
                .sharedDataType(sharedEmailPermission.getSharedEmail().getSharedDataType())
                .canEditorEditPermission(sharedEmailPermission.getSharedEmail().isCanEditorEditPermission())
                .canViewerViewToolMenu(sharedEmailPermission.getSharedEmail().isCanViewerViewToolMenu())
                .inviteePermissions(currentPermissions)
                .createdAt(sharedEmailPermission.getSharedEmail().getCreatedAt())
                .updatedAt(sharedEmailPermission.getSharedEmail().getUpdatedAt())
                .build();
    }
}
