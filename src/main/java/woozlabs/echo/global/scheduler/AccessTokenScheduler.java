package woozlabs.echo.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.member.repository.AccountRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessTokenScheduler {

    private final AccountRepository accountRepository;
    private final TokenRefreshExecutor tokenRefreshExecutor;

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void checkAndRefreshTokens() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(50);
        List<Long> expiredAccountIds = accountRepository.findExpiredAccountIds(cutoffTime);

        if (expiredAccountIds.isEmpty()) {
            log.debug("No expired tokens found.");
            return;
        }

        log.info("Found {} accounts with expired tokens. Starting refresh process.", expiredAccountIds.size());
        tokenRefreshExecutor.refreshTokensAsync(expiredAccountIds);
    }
}
