package woozlabs.echo.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.repository.AccountRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;
import woozlabs.echo.global.utils.GoogleOAuthUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenRefreshExecutor {

    private final AccountRepository accountRepository;
    private final GoogleOAuthUtils googleOAuthUtils;

    @Async
    public void refreshTokensAsync(List<Long> accountIds) {
        for (Long accountId : accountIds) {
            try {
                refreshTokenForAccount(accountId);
            } catch (Exception e) {
                log.error("Failed to refresh token for Account ID: {}", accountId, e);
            }
        }
    }

    @Transactional
    public void refreshTokenForAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));

        try {
            Map<String, String> newTokens = googleOAuthUtils.refreshAccessToken(account.getRefreshToken());
            String newAccessToken = newTokens.get("access_token");

            account.setAccessToken(newAccessToken);
            account.setAccessTokenFetchedAt(LocalDateTime.now());
            accountRepository.save(account);
            log.info("Successfully refreshed token for Account ID: {}", accountId);
        } catch (Exception e) {
            log.error("Failed to refresh token for Account ID: {}", accountId, e);
            throw new CustomErrorException(ErrorCode.FAILED_TO_REFRESH_GOOGLE_TOKEN,
                    "Failed to refresh token for Account ID: " + accountId, e);
        }
    }
}
