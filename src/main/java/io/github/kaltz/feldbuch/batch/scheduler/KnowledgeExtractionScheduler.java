package io.github.kaltz.feldbuch.batch.scheduler;

import io.github.kaltz.feldbuch.batch.config.KnowledgeExtractionBatchConfig;
import io.github.kaltz.feldbuch.conversation.reader.KnowledgeConversationReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeExtractionScheduler {

    private static final String BATCH_LOG =
            "[KNOWLEDGE_EXTRACTION_SCHEDULER]";

    private final JobLauncher jobLauncher;
    private final KnowledgeConversationReader conversationReader;

    @Qualifier(
            KnowledgeExtractionBatchConfig.JOB_NAME
    )
    private final Job knowledgeExtractionJob;

    /**
     * 30분마다 지식 추출 대화를 확인한다.
     */
    @Scheduled(
            fixedDelayString =
                    "${batch.knowledge-extraction.fixed-delay:12h}"
    )
    public void run() {

        if (!conversationReader.hasExtractionTarget()) {
            log.debug(
                    "{} skip. No extraction targets.",
                    BATCH_LOG
            );

            return;
        }

        JobParameters jobParameters =
                new JobParametersBuilder()
                        .addLong(
                                "requestedAt",
                                System.currentTimeMillis()
                        )
                        .toJobParameters();

        try {
            log.info(
                    "{} Job launch requested.",
                    BATCH_LOG
            );

            jobLauncher.run(
                    knowledgeExtractionJob,
                    jobParameters
            );
        } catch (Exception exception) {
            log.error(
                    "{} Job launch failed.",
                    BATCH_LOG,
                    exception
            );
        }
    }
}
