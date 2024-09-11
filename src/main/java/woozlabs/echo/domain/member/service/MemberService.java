package woozlabs.echo.domain.member.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.member.dto.AppearanceDto;
import woozlabs.echo.domain.member.dto.NotificationDto;
import woozlabs.echo.domain.member.dto.PreferenceDto;
import woozlabs.echo.domain.member.dto.UpdatePreferenceRequestDto;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.repository.AccountRepository;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void updatePreference(String uid, UpdatePreferenceRequestDto updatePreferenceRequest) {
        Account account = accountRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));

        Member member = account.getMember();

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

    public PreferenceDto getPreference(String uid) {
        Account account = accountRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));

        Member member = account.getMember();

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
    public void deleteMember(String uid) {
        Account account = accountRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));

        Member member = account.getMember();
        member.setDeletedAt(LocalDateTime.now());

        try {
            FirebaseAuth.getInstance().deleteUser(uid);
        } catch (FirebaseAuthException e) {
            throw new CustomErrorException(ErrorCode.FIREBASE_ACCOUNT_DELETION_ERROR, e.getMessage());
        }

        memberRepository.save(member);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void hardDeleteExpiredMembers() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Member> expiredMember = memberRepository.findAllByDeletedAtBefore(thirtyDaysAgo);

        memberRepository.deleteAll(expiredMember);
    }
}
