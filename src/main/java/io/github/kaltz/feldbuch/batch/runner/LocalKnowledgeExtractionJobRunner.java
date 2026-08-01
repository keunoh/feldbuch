package io.github.kaltz.feldbuch.batch.runner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("local")
@ConditionalOnProperty(
        name = "feldbuch.batch.knowledge-extraction.run",
        havingValue = "true"
)
public class LocalKnowledgeExtractionJobRunner implements ApplicationRunner {

    private final JobLauncher jobLauncher;
    private final Job knowledgeExtractionJob;

    public LocalKnowledgeExtractionJobRunner(
            JobLauncher jobLauncher,
            @Qualifier("knowledgeExtractionJob")
            Job knowledgeExtractionJob
    ) {
        this.jobLauncher = jobLauncher;
        this.knowledgeExtractionJob = knowledgeExtractionJob;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        JobParameters jobParameters =
                new JobParametersBuilder()
                        .addLong(
                                "executionTime",
                                System.currentTimeMillis()
                        )
                        .toJobParameters();

        log.info(
                "[KNOWLEDGE_EXTRACTION_JOB] Manual execution started."
        );

        JobExecution execution =
                jobLauncher.run(
                        knowledgeExtractionJob,
                        jobParameters
                );

        log.info(
                "[KNOWLEDGE_EXTRACTION_JOB] Manual execution completed. "
                        + "status={} exitStatus={}",
                execution.getStatus(),
                execution.getExitStatus()
        );
    }
}
