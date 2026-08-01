package io.github.kaltz.feldbuch.batch.config;

import io.github.kaltz.feldbuch.batch.tasklet.KnowledgeExtractionTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class KnowledgeExtractionBatchConfig {

    public static final String JOB_NAME =
            "knowledgeExtractionJob";

    public static final String STEP_NAME =
            "knowledgeExtractionStep";

    private final JobRepository jobRepository;

    private final PlatformTransactionManager transactionManager;

    private final KnowledgeExtractionTasklet knowledgeExtractionTasklet;

    @Bean
    public Job knowledgeExtractionJob() {

        return new JobBuilder(
                JOB_NAME,
                jobRepository
        )
                .start(knowledgeExtractionStep())
                .build();
    }

    @Bean
    public Step knowledgeExtractionStep() {

        return new StepBuilder(
                STEP_NAME,
                jobRepository
        )
                .tasklet(
                        knowledgeExtractionTasklet,
                        transactionManager
                )
                .build();
    }

}
