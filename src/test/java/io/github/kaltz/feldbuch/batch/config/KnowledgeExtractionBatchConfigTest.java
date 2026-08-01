package io.github.kaltz.feldbuch.batch.config;

import io.github.kaltz.feldbuch.batch.tasklet.KnowledgeExtractionTasklet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBatchTest
@SpringBootTest(
        properties = {
                "spring.batch.job.enabled=false"
        }
)
@ActiveProfiles("test")
class KnowledgeExtractionBatchConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("knowledgeExtractionJob")
    private Job knowledgeExtractionJob;

    @MockitoBean
    private KnowledgeExtractionTasklet knowledgeExtractionTasklet;

    @BeforeEach
    void setUp() {

        /**
         * 프로젝트에 Job이 여러 개 존재할 수 있으므로
         * 이번 테스트에서 실행할 Job을 명시적으로 설정한다.
         */
        jobLauncherTestUtils.setJob(
                knowledgeExtractionJob
        );
    }

    @Test
    void 지식_추출_배치_Job을_실행한다() throws Exception {

        // given
        when(
                knowledgeExtractionTasklet.execute(
                        any(),
                        any()
                )
        ).thenReturn(RepeatStatus.FINISHED);

        JobParameters jobParameters =
                new JobParametersBuilder()
                        .addLong(
                                "executionTime",
                                System.currentTimeMillis()
                        )
                        .toJobParameters();

        // when
        JobExecution jobExecution =
                jobLauncherTestUtils.launchJob(
                        jobParameters
                );

        // then
        assertThat(jobExecution.getStatus())
                .isEqualTo(BatchStatus.COMPLETED);

        assertThat(jobExecution.getExitStatus().getExitCode())
                .isEqualTo("COMPLETED");

        Collection<StepExecution> stepExecutions =
                jobExecution.getStepExecutions();

        assertThat(stepExecutions)
                .hasSize(1);

        StepExecution stepExecution =
                stepExecutions.iterator().next();

        assertThat(stepExecution.getStepName())
                .isEqualTo(
                        KnowledgeExtractionBatchConfig.STEP_NAME
                );

        assertThat(stepExecution.getStatus())
                .isEqualTo(BatchStatus.COMPLETED);

        verify(knowledgeExtractionTasklet)
                .execute(
                        any(),
                        any()
                );
    }
}