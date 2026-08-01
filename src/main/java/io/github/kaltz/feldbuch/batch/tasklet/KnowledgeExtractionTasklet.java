package io.github.kaltz.feldbuch.batch.tasklet;

import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.reader.KnowledgeConversationReader;
import io.github.kaltz.feldbuch.knowledge.service.KnowledgeExtractionService;
import io.github.kaltz.feldbuch.knowledge.service.KnowledgeExtractionStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeExtractionTasklet implements Tasklet {

    private static final String BATCH_LOG =
            "[KNOWLEDGE_EXTRACTION_BATCH]";

    private final KnowledgeConversationReader conversationReader;

    private final KnowledgeExtractionService extractionService;

    private final KnowledgeExtractionStatusService statusService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        List<Conversation> targets =
                conversationReader.findExtractionTargets();

        log.info(
                "{} Batch started. targetCount={}",
                BATCH_LOG,
                targets.size()
        );

        int successCount = 0;
        int failureCount = 0;

        for (Conversation conversation : targets) {

            Long conversationId = conversation.getId();
            Long userId = conversation.getUser().getId();

            try {
                statusService.start(conversationId);

                extractionService.extract(
                        userId,
                        conversationId
                );

                statusService.complete(conversationId);

                successCount++;

                log.info(
                        "{} Extraction completed. conversationId={} userId={}",
                        BATCH_LOG,
                        conversationId,
                        userId
                );
            } catch (Exception e) {
                failureCount++;

                markAsFailed(conversationId);

                log.error(
                        "{} Extraction failed. conversationId={} userId={}",
                        BATCH_LOG,
                        conversationId,
                        userId,
                        e
                );
            }
        }

        log.info(
                "{} Batch completed. targetCount={} successCount={} failureCount={}",
                BATCH_LOG,
                targets.size(),
                successCount,
                failureCount
        );

        return RepeatStatus.FINISHED;
    }

    private void markAsFailed(Long conversationId) {

        try {
            statusService.fail(conversationId);
        } catch (Exception statusException) {
            log.error(
                    "{} Failed to update extraction status. conversationId={}",
                    BATCH_LOG,
                    conversationId,
                    statusException
            );
        }
    }
}
