package woozlabs.echo.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.repository.AccountRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;
import woozlabs.echo.global.utils.GoogleOAuthUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessTokenScheduler {

    private final AccountRepository accountRepository;
    private final GoogleOAuthUtils googleOAuthUtils;

    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional(readOnly = true)
    public void checkAndRefreshTokens() {
        LocalDateTime cutoffTime = LocalDateTime.now().minus(50, ChronoUnit.MINUTES);
        List<Account> accounts = accountRepository.findAccountsByCutoffTime(cutoffTime);
        for (Account account : accounts) {
            refreshToken(account);
        }
    }

    @Transactional
    public void refreshToken(Account account) {
        try {
            Map<String, String> newTokens = googleOAuthUtils.refreshAccessToken(account.getRefreshToken());
            String newAccessToken = newTokens.get("access_token");

            account.setAccessToken(newAccessToken);
            account.setAccessTokenFetchedAt(LocalDateTime.now());
            accountRepository.save(account);
        } catch (Exception e) {
            log.error("Failed to refresh token for Account: {}", account.getId(), e);
            throw new CustomErrorException(ErrorCode.FAILED_TO_REFRESH_GOOGLE_TOKEN, "Failed to refresh token for Account: " + account.getId(), e);
        }
    }
}
