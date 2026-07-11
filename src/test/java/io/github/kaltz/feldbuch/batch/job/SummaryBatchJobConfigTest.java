package io.github.kaltz.feldbuch.batch.job;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
class SummaryBatchJobConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job summaryJob;

    @Test
    void summaryBatch() throws Exception {
        jobLauncherTestUtils.setJob(summaryJob);

        JobExecution execution = jobLauncherTestUtils.launchJob();

        assertThat(execution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
    }

}