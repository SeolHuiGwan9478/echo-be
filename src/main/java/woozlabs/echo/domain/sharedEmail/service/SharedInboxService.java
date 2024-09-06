package woozlabs.echo.domain.sharedEmail.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.repository.AccountRepository;
import woozlabs.echo.domain.sharedEmail.dto.SendSharedEmailInvitationDto;
import woozlabs.echo.domain.sharedEmail.dto.ShareEmailRequestDto;
import woozlabs.echo.domain.sharedEmail.entity.Permission;
import woozlabs.echo.domain.sharedEmail.entity.SharedEmail;
import woozlabs.echo.domain.sharedEmail.entity.SharedEmailPermission;
import woozlabs.echo.domain.sharedEmail.repository.SharedEmailPermissionRepository;
import woozlabs.echo.domain.sharedEmail.repository.SharedInboxRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SharedInboxService {

    private final SharedInboxRepository sharedInboxRepository;
    private final AccountRepository accountRepository;
    private final SharedEmailPermissionRepository sharedEmailPermissionRepository;
    private final InviteShareEmailService inviteShareEmailService;

    @Transactional
    public void publicShareEmail(String uid, ShareEmailRequestDto shareEmailRequestDto) {
        Account account = accountRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));

        List<String> invitees = shareEmailRequestDto.getInvitees();
        Map<String, Permission> inviteePermissions = new HashMap<>();

        SendSharedEmailInvitationDto sendSharedEmailInvitationDto = SendSharedEmailInvitationDto.builder()
                .invitationMemo(shareEmailRequestDto.getInvitationMemo())
                .access(shareEmailRequestDto.getAccess().name())
                .permission(shareEmailRequestDto.getPermission().name())
                .dataId(shareEmailRequestDto.getDataId())
                .sharedDataType(shareEmailRequestDto.getSharedDataType().name())
                .build();

        if (shareEmailRequestDto.isNotifyInvitation()) {
            for (String invitee : invitees) {
                accountRepository.findByEmail(invitee).ifPresentOrElse(
                        existingAccount -> {
                            inviteePermissions.put(invitee, shareEmailRequestDto.getPermission());
                            inviteShareEmailService.sendEmailViaSES(existingAccount.getEmail(), shareEmailRequestDto.getInvitationMemo(), sendSharedEmailInvitationDto);
                        },
                        () -> {
                            inviteePermissions.put(invitee, Permission.VIEWER);
                            inviteShareEmailService.sendEmailViaSES(invitee, "This email grants access to this item without logging in. Only forward it to people you trust.\n" + shareEmailRequestDto.getInvitationMemo(), sendSharedEmailInvitationDto);
                        }
                );
            }
        } else {
            for (String invitee : invitees) {
                inviteePermissions.put(invitee, shareEmailRequestDto.getPermission());
            }
        }

        SharedEmail sharedEmail = SharedEmail.builder()
                .access(shareEmailRequestDto.getAccess())
                .dataId(shareEmailRequestDto.getDataId())
                .sharedDataType(shareEmailRequestDto.getSharedDataType())
                .owner(account)
                .canEditorEditPermission(shareEmailRequestDto.isCanEditorEditPermission())
                .canViewerViewToolMenu(shareEmailRequestDto.isCanViewerViewToolMenu())
                .build();

        sharedInboxRepository.save(sharedEmail);

        SharedEmailPermission sharedEmailPermission = SharedEmailPermission.builder()
                .sharedEmailId(sharedEmail.getId())
                .inviteePermissions(inviteePermissions)
                .build();

        sharedEmailPermissionRepository.save(sharedEmailPermission);
    }
}
