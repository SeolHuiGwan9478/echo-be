package woozlabs.echo.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.entity.SubAccount;
import woozlabs.echo.domain.member.entity.SuperAccount;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.domain.member.repository.SubAccountRepository;
import woozlabs.echo.domain.member.repository.SuperAccountRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;
import woozlabs.echo.global.utils.GoogleOAuthUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TokenSchedulerService {

    private final MemberRepository memberRepository;
    private final SuperAccountRepository superAccountRepository;
    private final SubAccountRepository subAccountRepository;
    private final GoogleOAuthUtils googleOAuthUtils;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkAndRefreshTokens() {
        LocalDateTime cutoffTime = LocalDateTime.now().minus(57, ChronoUnit.MINUTES);

        List<Member> members = memberRepository.findMembersWithAccountsByCutoffTime(cutoffTime);
        for (Member member : members) {
            refreshToken(member);

            SuperAccount superAccount = member.getSuperAccount();
            if (superAccount != null && shouldRefreshToken(superAccount.getAccessTokenFetchedAt())) {
                refreshToken(superAccount);

                List<SubAccount> subAccounts = superAccount.getSubAccounts();
                for (SubAccount subAccount : subAccounts) {
                    if (shouldRefreshToken(subAccount.getAccessTokenFetchedAt())) {
                        refreshToken(subAccount);
                    }
                }
            }
        }
    }

    private boolean shouldRefreshToken(LocalDateTime tokenFetchedAt) {
        if (tokenFetchedAt == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        long minutesElapsed = ChronoUnit.MINUTES.between(tokenFetchedAt, now);

        return minutesElapsed >= 57;
    }

    public void refreshToken(Member member) {
        try {
            Map<String, String> newtokens = googleOAuthUtils.refreshAccessToken(member.getRefreshToken());
            String newAccessToken = newtokens.get("access_token");

            member.setAccessToken(newAccessToken);
            member.setAccessTokenFetchedAt(LocalDateTime.now());
            memberRepository.save(member);
        } catch (Exception e) {
            throw new CustomErrorException(ErrorCode.FAILED_TO_REFRESH_GOOGLE_TOKEN);
        }
    }

    private void refreshToken(SuperAccount superAccount) {
        try {
            Map<String, String> newTokens = googleOAuthUtils.refreshAccessToken(superAccount.getRefreshToken());
            String newAccessToken = newTokens.get("access_token");

            superAccount.setAccessToken(newAccessToken);
            superAccount.setAccessTokenFetchedAt(LocalDateTime.now());

            superAccountRepository.save(superAccount);
        } catch (Exception e) {
            throw new CustomErrorException(ErrorCode.FAILED_TO_REFRESH_GOOGLE_TOKEN);
        }
    }

    private void refreshToken(SubAccount subAccount) {
        try {
            Map<String, String> newTokens = googleOAuthUtils.refreshAccessToken(subAccount.getRefreshToken());
            String newAccessToken = newTokens.get("access_token");

            subAccount.setAccessToken(newAccessToken);
            subAccount.setAccessTokenFetchedAt(LocalDateTime.now());

            subAccountRepository.save(subAccount);
        } catch (Exception e) {
            throw new CustomErrorException(ErrorCode.FAILED_TO_REFRESH_GOOGLE_TOKEN);
        }
    }

}
