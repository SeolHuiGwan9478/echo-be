package woozlabs.echo.domain.member.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.member.dto.AppearanceDto;
import woozlabs.echo.domain.member.dto.NotificationDto;
import woozlabs.echo.domain.member.dto.PreferenceDto;
import woozlabs.echo.domain.member.dto.UpdatePreferenceRequestDto;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.entity.MemberAccount;
import woozlabs.echo.domain.member.repository.AccountRepository;
import woozlabs.echo.domain.member.repository.MemberAccountRepository;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final MemberAccountRepository memberAccountRepository;

    @Transactional
    public void updatePreference(String primaryUid, UpdatePreferenceRequestDto updatePreferenceRequest) {
        Member member = memberRepository.findByPrimaryUid(primaryUid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER));

        PreferenceDto preferenceDto = updatePreferenceRequest.getPreference();
        if (preferenceDto != null) {
            if (preferenceDto.getLanguage() != null) {
                member.setLanguage(preferenceDto.getLanguage());
            }

            AppearanceDto appearanceDto = preferenceDto.getAppearance();
            if (appearanceDto != null) {
                if (appearanceDto.getTheme() != null) {
                    member.setTheme(appearanceDto.getTheme());
                }
                if (appearanceDto.getDensity() != null) {
                    member.setDensity(appearanceDto.getDensity());
                }
            }

            NotificationDto notificationDto = preferenceDto.getNotification();
            if (notificationDto != null) {
                if (notificationDto.getWatchNotification() != null) {
                    member.setWatchNotification(notificationDto.getWatchNotification());
                }
                if (notificationDto.getMarketingEmails() != null) {
                    member.setMarketingEmails(notificationDto.getMarketingEmails());
                }
                if (notificationDto.getSecurityEmails() != null) {
                    member.setSecurityEmails(notificationDto.getSecurityEmails());
                }
            }
        }
    }

    public PreferenceDto getPreference(String primaryUid) {
        Member member = memberRepository.findByPrimaryUid(primaryUid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER));

        return PreferenceDto.builder()
                .language(member.getLanguage())
                .appearance(AppearanceDto.builder()
                        .theme(member.getTheme())
                        .density(member.getDensity())
                        .build())
                .notification(NotificationDto.builder()
                        .watchNotification(member.getWatchNotification())
                        .marketingEmails(member.isMarketingEmails())
                        .securityEmails(member.isSecurityEmails())
                        .build())
                .build();
    }

    @Transactional
    public void deleteMember(String primaryUid) {
        Member member = memberRepository.findByPrimaryUid(primaryUid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER));

        member.setDeletedAt(LocalDateTime.now());

        List<MemberAccount> memberAccounts = member.getMemberAccounts();
        for (MemberAccount memberAccount : memberAccounts) {
            Account account = memberAccount.getAccount();
            account.getMemberAccounts().remove(memberAccount);
            accountRepository.save(account);
        }

        member.getMemberAccounts().clear();
        memberRepository.save(member);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void hardDeleteExpiredMembers() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Member> expiredMember = memberRepository.findAllByDeletedAtBefore(thirtyDaysAgo);

        for (Member member : expiredMember) {
            try {
                FirebaseAuth.getInstance().deleteUser(member.getPrimaryUid());
            } catch (FirebaseAuthException e) {
                throw new CustomErrorException(ErrorCode.FIREBASE_ACCOUNT_DELETION_ERROR, e.getMessage());
            }
        }

        memberRepository.deleteAll(expiredMember);
    }

    @Transactional
    public void hardDeleteMember(String primaryUid) {
        Member member = memberRepository.findByPrimaryUid(primaryUid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER));

        try {
            FirebaseAuth.getInstance().deleteUser(member.getPrimaryUid());
        } catch (FirebaseAuthException e) {
            throw new CustomErrorException(ErrorCode.FIREBASE_ACCOUNT_DELETION_ERROR, e.getMessage());
        }

        List<Account> accountsToDelete = new ArrayList<>();

        List<MemberAccount> memberAccounts = new ArrayList<>(member.getMemberAccounts());
        for (MemberAccount memberAccount : memberAccounts) {
            Account account = memberAccount.getAccount();
            account.getMemberAccounts().remove(memberAccount);
            member.getMemberAccounts().remove(memberAccount);

            memberAccountRepository.delete(memberAccount);

            // 해당 계정이 다른 멤버와 연결되어 있지 않으면 삭제 대상에 추가
            if (account.getMemberAccounts().isEmpty()) {
                accountsToDelete.add(account);
            }
        }

        memberRepository.delete(member);
        accountRepository.deleteAll(accountsToDelete);

        log.info("Successfully deleted member with UID: {}", primaryUid);
    }
}
