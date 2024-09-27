package woozlabs.echo.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.repository.AccountRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenRefreshExecutor {

    private final AccountRepository accountRepository;
    private final TokenRefreshTask tokenRefreshTask;

    public void refreshTokensAsync(List<Long> accountIds) {
        List<Account> accounts = accountRepository.findAllById(accountIds);

        for (Account account : accounts) {
            try {
                tokenRefreshTask.refreshTokenForAccount(account);
            } catch (Exception e) {
                log.error("Failed to refresh token for Account ID: {}", account.getId(), e);
            }
        }
    }
}
