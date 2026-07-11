package io.github.kaltz.feldbuch.batch.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class SummaryBatchJobConfig {

    private final JobRepository jobRepository;

    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job summaryJob() {
        return new JobBuilder("summaryJob", jobRepository)
                .start(summaryStep())
                .build();
    }

    @Bean
    public Step summaryStep() {
        return new StepBuilder("summaryStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("Summary Batch Start");

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
