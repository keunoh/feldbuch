package io.github.kaltz.feldbuch.knowledge.service;

import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeMergeResponse;
import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeSummaryResponse;
import io.github.kaltz.feldbuch.ai.service.AiKnowledgeMergeService;
import io.github.kaltz.feldbuch.ai.service.AiKnowledgeSummaryService;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.reader.ConversationReader;
import io.github.kaltz.feldbuch.knowledge.context.ConversationAiContext;
import io.github.kaltz.feldbuch.knowledge.context.ConversationAiContextBuilder;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNoteType;
import io.github.kaltz.feldbuch.knowledge.repository.KnowledgeNoteRepository;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.reader.UserReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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

    private final AiKnowledgeSummaryService aiKnowledgeSummaryService;

    private final AiKnowledgeMergeService aiKnowledgeMergeService;

    private final KnowledgeNoteCommandService knowledgeNoteCommandService;

    private final KnowledgeNoteRepository knowledgeNoteRepository;

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
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

        /*
         * 새롭게 추가된 메시지 범위를 먼저 요약한다.
         * 이 응답은 Incremental 노트 생성과
         * 최초 Consolidated 노트 생성에 함께 사용한다.
         */
        AiKnowledgeSummaryResponse summaryResponse =
                aiKnowledgeSummaryService.summarize(
                        context.content()
                );

        /*
         * 배치가 실행될 때마다 새로운 Incremental 노트를 생성한다.
         */
        KnowledgeNote incrementalNote =
                knowledgeNoteCommandService.saveIncremental(
                        user,
                        conversation,
                        summaryResponse
                );

        /*
         * 같은 대화의 Consolidated 노트가 있으면 갱신하고,
         * 없다면 이번 요약 결과로 최초 생성한다.
         */
        KnowledgeNote consolidatedNote =
                knowledgeNoteRepository
                        .findFirstByUserIdAndConversationIdAndType(
                                userId,
                                conversationId,
                                KnowledgeNoteType.CONSOLIDATED
                        )
                        .map(existingNote ->
                                mergeConsolidatedNote(
                                        user,
                                        existingNote,
                                        incrementalNote
                                )
                        )
                        .orElseGet(() ->
                                knowledgeNoteCommandService
                                        .saveConsolidated(
                                                user,
                                                conversation,
                                                summaryResponse
                                        )
                        );

        conversation.completeKnowledgeExtraction(
                context.lastMessageId()
        );

        log.info(
                "{} Completed. conversationId={} incrementalNoteId={} consolidatedNoteId={} checkpointMessageId={}",
                EXTRACTION_LOG,
                conversationId,
                incrementalNote.getId(),
                consolidatedNote.getId(),
                context.lastMessageId()
        );

        /*
         * 배치의 대표 결과로는 통합 노트를 반환한다.
         */
        return consolidatedNote;
    }

    private KnowledgeNote mergeConsolidatedNote(
            User user,
            KnowledgeNote consolidatedNote,
            KnowledgeNote incrementalNote
    ) {
        AiKnowledgeMergeResponse mergeResponse =
                aiKnowledgeMergeService.merge(
                        consolidatedNote,
                        incrementalNote
                );

        return knowledgeNoteCommandService
                .updateConsolidated(
                        user,
                        consolidatedNote,
                        mergeResponse
                );
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

        if (
                context.content() == null
                        || context.content().isBlank()
        ) {
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
                conversation.getLastExtractedMessageId();

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
