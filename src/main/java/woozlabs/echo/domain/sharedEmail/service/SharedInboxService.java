package woozlabs.echo.domain.sharedEmail.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetResponse;
import woozlabs.echo.domain.gmail.service.GmailService;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.repository.AccountRepository;
import woozlabs.echo.domain.sharedEmail.dto.*;
import woozlabs.echo.domain.sharedEmail.entity.Permission;
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

        SharedEmail sharedEmail = SharedEmail.builder()
                .access(createSharedRequestDto.getAccess())
                .dataId(createSharedRequestDto.getDataId())
                .sharedDataType(createSharedRequestDto.getSharedDataType())
                .owner(account)
                .canEditorEditPermission(createSharedRequestDto.isCanEditorEditPermission())
                .canViewerViewToolMenu(createSharedRequestDto.isCanViewerViewToolMenu())
                .build();

        sharedInboxRepository.save(sharedEmail);

        Map<String, Permission> inviteePermissions = new HashMap<>();
        inviteePermissions.put(account.getEmail(), Permission.OWNER);

        SharedEmailPermission sharedEmailPermission = SharedEmailPermission.builder()
                .sharedEmail(sharedEmail)
                .inviteePermissions(inviteePermissions)
                .build();

        sharedEmailPermissionRepository.save(sharedEmailPermission);

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
                            newInviteePermissions.put(invitee, Permission.VIEWER);
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

    public GetSharedEmailResponseDto getSharedEmail(String uid, String dataId) {
        log.info("Fetching shared email for UID: {}", uid);

        SharedEmail sharedEmail = sharedInboxRepository.findByDataId(dataId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SHARED_EMAIL));

        Permission permissionLevel;

        if (uid != null) {
            Account account = accountRepository.findByUid(uid)
                    .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));

            SharedEmailPermission sharedEmailPermission = sharedEmailPermissionRepository.findBySharedEmailId(sharedEmail.getId())
                    .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SHARED_EMAIL_PERMISSION));

            if (!sharedEmailPermission.getInviteePermissions().containsKey(account.getEmail())) {
                log.error("Forbidden access to shared email for account: {}", account.getEmail());
                throw new CustomErrorException(ErrorCode.FORBIDDEN_ACCESS_TO_SHARED_EMAIL);
            }

            permissionLevel = sharedEmailPermission.getInviteePermissions().getOrDefault(account.getEmail(), Permission.VIEWER);
        } else {
            permissionLevel = Permission.VIEWER;
        }


        GmailThreadGetResponse gmailThreadResponse;
        try {
            gmailThreadResponse = gmailService.getUserEmailThread(sharedEmail.getOwner().getUid(), sharedEmail.getDataId());
        } catch (CustomErrorException e) {
            if (e.getErrorCode() == ErrorCode.NOT_FOUND_GMAIL_THREAD) {
                sharedInboxRepository.delete(sharedEmail);
                log.warn("Shared email deleted for dataId: {} due to missing Gmail thread", sharedEmail.getDataId());
                throw new CustomErrorException(ErrorCode.THREAD_NOT_FOUND_AND_REMOVED, e.getMessage());
            }
            throw e;
        }

        GetSharedEmailResponseDto responseDto = GetSharedEmailResponseDto.builder()
                .gmailThreadGetResponse(gmailThreadResponse)
                .permissionLevel(permissionLevel)
                .canEdit(permissionLevel == Permission.EDITOR && sharedEmail.isCanEditorEditPermission())
                .canViewToolMenu(permissionLevel == Permission.EDITOR || permissionLevel == Permission.OWNER && sharedEmail.isCanViewerViewToolMenu())
                .build();

        if (permissionLevel == Permission.OWNER) {
            Map<String, Permission> inviteePermissions = sharedEmailPermissionRepository.findBySharedEmailId(sharedEmail.getId())
                    .map(SharedEmailPermission::getInviteePermissions)
                    .orElse(new HashMap<>());
            responseDto.setInviteePermissions(inviteePermissions);
        }

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
}
