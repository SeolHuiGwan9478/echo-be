package woozlabs.echo.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessTokenScheduler {

    private final JobLauncher jobLauncher;
    private final Job refreshTokenJob;

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void runBatchJob() {
        log.info("Starting batch job to refresh tokens at {}", LocalDateTime.now());
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("time", LocalDateTime.now().toString())
                    .toJobParameters();
            jobLauncher.run(refreshTokenJob, jobParameters);
            log.info("Batch job submitted successfully.");
        } catch (Exception e) {
            log.error("Error occurred while running the job", e);
        }
    }
}
