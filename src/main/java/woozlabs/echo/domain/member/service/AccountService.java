package woozlabs.echo.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.member.dto.*;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.entity.Theme;
import woozlabs.echo.domain.member.repository.AccountRepository;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public ProfileResponseDto getProfileByField(String fieldType, String fieldValue) {
        Account account = fetchMemberByField(fieldType, fieldValue);

        return ProfileResponseDto.builder()
                .uid(account.getUid())
                .provider(account.getProvider())
                .displayName(account.getDisplayName())
                .profileImageUrl(account.getProfileImageUrl())
                .email(account.getEmail())
                .isPrimary(account.isPrimary())
                .build();
    }

    private Account fetchMemberByField(String fieldType, String fieldValue) {
        if (fieldType.equals("email")) {
            return accountRepository.findByEmail(fieldValue)
                    .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));
        } else if (fieldType.equals("uid")) {
            return accountRepository.findByUid(fieldValue)
                    .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));
        } else {
            throw new CustomErrorException(ErrorCode.INVALID_FIELD_TYPE_ERROR_MESSAGE);
        }
    }

    @Transactional
    public void updatePreference(String uid, UpdatePreferenceRequestDto updatePreferenceRequest) {
        Account account = accountRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));

        Member member = account.getMember();
        if (member == null) {
            throw new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE);
        }

        PreferenceDto preferenceDto = updatePreferenceRequest.getPreference();
        AppearanceDto appearanceDto = preferenceDto.getAppearance();
        NotificationDto notificationDto = preferenceDto.getNotification();

        member.setLanguage(preferenceDto.getLang() != null ? preferenceDto.getLang() : "en");
        member.setTheme(appearanceDto.getTheme() != null ? appearanceDto.getTheme() : Theme.SYSTEM);
        member.setWatchNotification(notificationDto.getWatchNotification());
        member.setMarketingEmails(notificationDto.isMarketingEmails());
        member.setSecurityEmails(notificationDto.isSecurityEmails());

        accountRepository.save(account);
    }


    // memberSErvice로 옮길것
//    public PreferenceDto getPreference(String uid) {
//        Member member = memberRepository.findByUid(uid);
//
//        return PreferenceDto.builder()
//                .lang(member.getLanguage())
//                .appearance(AppearanceDto.builder()
//                        .theme(member.getTheme())
//                        .build())
//                .notification(NotificationDto.builder()
//                        .watchNotification(member.getWatchNotification())
//                        .marketingEmails(member.isMarketingEmails())
//                        .securityEmails(member.isSecurityEmails())
//                        .build())
//                .build();
//    }
}
