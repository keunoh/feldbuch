package io.github.kaltz.feldbuch.knowledge.service;

import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeSummaryResponse;
import io.github.kaltz.feldbuch.ai.service.AiKnowledgeSummaryService;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.reader.ConversationReader;
import io.github.kaltz.feldbuch.knowledge.context.ConversationAiContext;
import io.github.kaltz.feldbuch.knowledge.context.ConversationAiContextBuilder;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.reader.UserReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeExtractionService {

    private static final String EXTRACTION_LOG =
            "[KNOWLEDGE_EXTRACTION]";

    private final UserReader userReader;

    private final ConversationReader conversationReader;

    private final ConversationAiContextBuilder contextBuilder;

    private final AiKnowledgeSummaryService
            aiKnowledgeSummaryService;

    private final KnowledgeNoteCommandService
            knowledgeNoteCommandService;


    @Transactional
    public KnowledgeNote extract(
            Long userId,
            Long conversationId
    ) {
        User user =
                userReader.get(userId);

        Conversation conversation =
                conversationReader.get(
                        userId,
                        conversationId
                );

        ConversationAiContext context =
                contextBuilder.build(
                        conversation
                );

        validateContext(
                conversation,
                context
        );

        log.info(
                "{} Started. conversationId={} fromMessageId={} toMessageId={} messageCount={}",
                EXTRACTION_LOG,
                conversationId,
                context.firstMessageId(),
                context.lastMessageId(),
                context.messageCount()
        );

        AiKnowledgeSummaryResponse response =
                aiKnowledgeSummaryService.summarize(
                        context.content()
                );

        KnowledgeNote note =
                knowledgeNoteCommandService
                        .saveAiSummary(
                                user,
                                conversation,
                                response
                        );

        conversation.completeKnowledgeExtraction(
                context.lastMessageId()
        );

        log.info(
                "{} Completed. conversationId={} checkpointMessageId={}",
                EXTRACTION_LOG,
                conversationId,
                context.lastMessageId()
        );

        return note;
    }

    private void validateContext(
            Conversation conversation,
            ConversationAiContext context
    ) {
        if (context == null) {
            throw new IllegalStateException(
                    "대화 AI 컨텍스트를 생성하지 못했습니다."
            );
        }

        if (context.isEmpty()) {
            throw new IllegalStateException(
                    "지식으로 추출할 새로운 대화 메시지가 없습니다."
            );
        }

        if (context.content().isBlank()) {
            throw new IllegalStateException(
                    "지식으로 추출할 대화 내용이 없습니다."
            );
        }

        Long checkpoint =
                context.lastMessageId();

        if (checkpoint == null) {
            throw new IllegalStateException(
                    "지식 추출 체크포인트 메시지가 없습니다."
            );
        }

        Long previousCheckpoint =
                conversation
                        .getLastExtractedMessageId();

        if (
                previousCheckpoint != null
                        && checkpoint <= previousCheckpoint
        ) {
            throw new IllegalStateException(
                    "새로운 지식 추출 범위가 없습니다."
            );
        }
    }
}
