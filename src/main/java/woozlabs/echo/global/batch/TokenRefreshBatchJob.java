package woozlabs.echo.global.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.repository.AccountRepository;
import woozlabs.echo.global.utils.GoogleOAuthUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class TokenRefreshBatchJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AccountRepository accountRepository;
    private final GoogleOAuthUtils googleOAuthUtils;

    private static final int CHUNK_SIZE = 10;

    @Bean
    public Job refreshTokenJob() {
        return new JobBuilder("refreshTokenJob", jobRepository)
                .start(refreshTokenStep())
                .build();
    }

    @Bean
    public Step refreshTokenStep() {
        return new StepBuilder("refreshTokenStep", jobRepository)
                .<Account, Account>chunk(CHUNK_SIZE, transactionManager)
                .reader(accountReader())
                .processor(tokenRefreshProcessor())
                .writer(accountWriter())
                .faultTolerant()
                .retry(Exception.class)
                .retryLimit(3)
                .build();
    }

    @Bean
    @StepScope
    public RepositoryItemReader<Account> accountReader() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(50);
        log.info("TokenRefreshBatchJob: Cutoff time for accessTokenFetchedAtBefore query: {}", cutoffTime);

        return new RepositoryItemReaderBuilder<Account>()
                .name("accountReader")
                .repository(accountRepository)
                .methodName("findByAccessTokenFetchedAtBefore")
                .arguments(cutoffTime)
                .pageSize(CHUNK_SIZE)
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<Account, Account> tokenRefreshProcessor() {
        return account -> {
            log.info("TokenRefreshBatchJob: Processing account ID: {}", account.getId());
            try {
                Map<String, String> newTokens = googleOAuthUtils.refreshAccessToken(account.getRefreshToken());
                String newAccessToken = newTokens.get("access_token");
                account.setAccessToken(newAccessToken);
                account.setAccessTokenFetchedAt(LocalDateTime.now());
                log.info("TokenRefreshBatchJob: Successfully refreshed token for Account ID: {}", account.getId());
                return account;
            } catch (Exception e) {
                log.error("TokenRefreshBatchJob: Failed to refresh token for Account ID: {}", account.getId(), e);
                throw e; // Rethrow the exception to trigger retry
            }
        };
    }

    @Bean
    public ItemWriter<Account> accountWriter() {
        return accounts -> {
            for (Account account : accounts) {
                if (account != null) {
                    log.info("TokenRefreshBatchJob: Saving account ID: {}", account.getId());
                    accountRepository.save(account);
                } else {
                    log.warn("TokenRefreshBatchJob: Received null account to save.");
                }
            }
        };
    }
}
