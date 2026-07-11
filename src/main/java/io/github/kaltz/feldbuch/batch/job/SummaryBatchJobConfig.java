package io.github.kaltz.feldbuch.batch.job;

import io.github.kaltz.feldbuch.batch.processor.SummaryItemProcessor;
import io.github.kaltz.feldbuch.batch.reader.SummaryItemReader;
import io.github.kaltz.feldbuch.batch.writer.SummaryItemWriter;
import io.github.kaltz.feldbuch.note.entity.Note;
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
public class SummaryBatchJobConfig {

    private final JobRepository jobRepository;

    private final PlatformTransactionManager transactionManager;

    private final SummaryItemReader summaryItemReader;
    private final SummaryItemProcessor summaryItemProcessor;
    private final SummaryItemWriter summaryItemWriter;

    @Bean
    public Job summaryJob() {
        return new JobBuilder("summaryJob", jobRepository)
                .start(summaryStep())
                .build();
    }

    @Bean
    public Step summaryStep() {
        return new StepBuilder("summaryStep", jobRepository)
                // 입력 Note, 출력 Note
                .<Note, Note>chunk(10, transactionManager)
                .reader(summaryItemReader.reader())
                .processor(summaryItemProcessor)
                .writer(summaryItemWriter)
                .build();
    }
}
