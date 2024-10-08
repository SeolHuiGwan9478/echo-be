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

    @Transactional
    public SharedEmailResponseDto createSharePost(String uid, CreateSharedRequestDto createSharedRequestDto) {
        Account account = accountRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));

        Optional<SharedEmail> existingSharedEmail = sharedInboxRepository.findByDataId(createSharedRequestDto.getDataId());
        if (existingSharedEmail.isPresent()) {
            log.info("SharedEmail already exists for dataId: {}", createSharedRequestDto.getDataId());
            SharedEmail sharedEmail = existingSharedEmail.get();

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

        log.info("Creating new SharedEmail for dataId: {}", createSharedRequestDto.getDataId());
        SharedEmail sharedEmail = SharedEmail.builder()
                .access(createSharedRequestDto.getAccess())
                .dataId(createSharedRequestDto.getDataId())
                .sharedDataType(createSharedRequestDto.getSharedDataType())
                .owner(account)
                .canEditorEditPermission(createSharedRequestDto.isCanEditorEditPermission())
                .canViewerViewToolMenu(createSharedRequestDto.isCanViewerViewToolMenu())
                .build();

        sharedInboxRepository.save(sharedEmail);
        log.info("New SharedEmail saved with id: {}", sharedEmail.getId());

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

        Object sharedEmailData;
        try {
            if (sharedEmail.getSharedDataType() == SharedDataType.THREAD) {
                sharedEmailData = gmailService.getUserEmailThread(sharedEmailPermission.getSharedEmail().getOwner().getUid(), sharedEmailPermission.getSharedEmail().getDataId());
            } else if (sharedEmail.getSharedDataType() == SharedDataType.MESSAGE) {
                sharedEmailData = gmailService.getUserEmailMessage(sharedEmailPermission.getSharedEmail().getOwner().getUid(), sharedEmailPermission.getSharedEmail().getDataId());
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

        Map<String, Permission> inviteePermissions = new HashMap<>();
        for (Map.Entry<String, Permission> entry : sharedEmailPermission.getInviteePermissions().entrySet()) {
            String inviteeEmail = entry.getKey();
            Permission originalPermission = entry.getValue();

            if (accountRepository.findByEmail(inviteeEmail).isPresent()) {
                inviteePermissions.put(inviteeEmail, originalPermission);
            } else {
                inviteePermissions.put(inviteeEmail, Permission.PUBLIC_VIEWER);
            }
        }

        Map<String, Permission> filteredPermissions = new HashMap<>();
        if (permissionLevel == Permission.OWNER || permissionLevel == Permission.EDITOR) {
            filteredPermissions = inviteePermissions;
        } else if (permissionLevel == Permission.VIEWER) {
            for (Map.Entry<String, Permission> entry : inviteePermissions.entrySet()) {
                String inviteeEmail = entry.getKey();
                Permission originalPermission = entry.getValue();

                if (inviteeEmail.equals(sharedEmailPermission.getSharedEmail().getOwner().getEmail()) || (account != null && inviteeEmail.equals(account.getEmail()))) {
                    filteredPermissions.put(inviteeEmail, originalPermission);
                }
            }
            filteredPermissions.put(sharedEmailPermission.getSharedEmail().getOwner().getEmail(), Permission.OWNER);
            if (account != null) {
                filteredPermissions.put(account.getEmail(), Permission.VIEWER);
            }
        } else if (permissionLevel == Permission.PUBLIC_VIEWER) {
            for (Map.Entry<String, Permission> entry : inviteePermissions.entrySet()) {
                String inviteeEmail = entry.getKey();
                if (inviteeEmail.equals(sharedEmailPermission.getSharedEmail().getOwner().getEmail())) {
                    filteredPermissions.put(inviteeEmail, entry.getValue());
                }
            }
            filteredPermissions.put(sharedEmailPermission.getSharedEmail().getOwner().getEmail(), Permission.OWNER);
        }

        GetSharedEmailResponseDto responseDto = GetSharedEmailResponseDto.builder()
                .sharedEmailData(sharedEmailData)
                .dataId(sharedEmailPermission.getSharedEmail().getDataId())
                .permissionLevel(permissionLevel)
                .canEdit(permissionLevel == Permission.EDITOR && sharedEmailPermission.getSharedEmail().isCanEditorEditPermission())
                .canViewToolMenu(permissionLevel == Permission.EDITOR || (permissionLevel == Permission.OWNER && sharedEmailPermission.getSharedEmail().isCanViewerViewToolMenu()))
                .sharedDataType(sharedEmailPermission.getSharedEmail().getSharedDataType())
                .build();

        responseDto.setInviteePermissions(filteredPermissions);

        return responseDto;
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
