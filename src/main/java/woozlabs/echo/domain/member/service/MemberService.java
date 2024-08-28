package woozlabs.echo.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.member.dto.AppearanceDto;
import woozlabs.echo.domain.member.dto.NotificationDto;
import woozlabs.echo.domain.member.dto.PreferenceDto;
import woozlabs.echo.domain.member.dto.UpdatePreferenceRequestDto;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.entity.Theme;
import woozlabs.echo.domain.member.repository.AccountRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final AccountRepository accountRepository;

    @Transactional
    public void updatePreference(String uid, UpdatePreferenceRequestDto updatePreferenceRequest) {
        Account account = accountRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));

        Member member = account.getMember();

        PreferenceDto preferenceDto = updatePreferenceRequest.getPreference();
        AppearanceDto appearanceDto = preferenceDto.getAppearance();
        NotificationDto notificationDto = preferenceDto.getNotification();

        member.setLanguage(preferenceDto.getLang() != null ? preferenceDto.getLang() : "en");
        member.setTheme(appearanceDto.getTheme() != null ? appearanceDto.getTheme() : Theme.SYSTEM);
        member.setWatchNotification(notificationDto.getWatchNotification());
        member.setMarketingEmails(notificationDto.isMarketingEmails());
        member.setSecurityEmails(notificationDto.isSecurityEmails());
    }

    public PreferenceDto getPreference(String uid) {
        Account account = accountRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));

        Member member = account.getMember();

        return PreferenceDto.builder()
                .lang(member.getLanguage())
                .appearance(AppearanceDto.builder()
                        .theme(member.getTheme())
                        .build())
                .notification(NotificationDto.builder()
                        .watchNotification(member.getWatchNotification())
                        .marketingEmails(member.isMarketingEmails())
                        .securityEmails(member.isSecurityEmails())
                        .build())
                .build();
    }
}
