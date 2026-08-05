package io.github.kaltz.feldbuch.knowledge.service;

import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeMergeResponse;
import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeSummaryResponse;
import io.github.kaltz.feldbuch.ai.service.AiKnowledgeMergeService;
import io.github.kaltz.feldbuch.ai.service.AiKnowledgeSummaryService;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.entity.ConversationMessage;
import io.github.kaltz.feldbuch.conversation.reader.ConversationReader;
import io.github.kaltz.feldbuch.knowledge.context.ConversationAiContext;
import io.github.kaltz.feldbuch.knowledge.context.ConversationAiContextBuilder;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNoteType;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeRootCategory;
import io.github.kaltz.feldbuch.knowledge.repository.KnowledgeNoteRepository;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.reader.UserReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnowledgeExtractionServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long CONVERSATION_ID = 1L;

    @Mock
    private UserReader userReader;

    @Mock
    private ConversationReader conversationReader;

    @Mock
    private ConversationAiContextBuilder contextBuilder;

    @Mock
    private AiKnowledgeSummaryService aiKnowledgeSummaryService;

    @Mock
    private AiKnowledgeMergeService aiKnowledgeMergeService;

    @Mock
    private KnowledgeNoteCommandService knowledgeNoteCommandService;

    @Mock
    private KnowledgeNoteRepository knowledgeNoteRepository;

    @Mock
    private User user;

    @Mock
    private Conversation conversation;

    @Mock
    private ConversationMessage userMessage;

    @Mock
    private ConversationMessage assistantMessage;

    @Mock
    private KnowledgeNote incrementalNote;

    @Mock
    private KnowledgeNote consolidatedNote;

    @InjectMocks
    private KnowledgeExtractionService knowledgeExtractionService;

    @Test
    void 통합_노트가_없으면_증분_노트와_통합_노트를_새로_생성한다() {
        // given
        String conversationContent = """
                USER:
                Spring Batch가 무엇인지 설명해줘.
                
                AI:
                Spring Batch는 대용량 일괄 처리를 위한 프레임워크입니다.
                """.trim();

        ConversationAiContext context =
                createContext(
                        conversationContent,
                        10L,
                        11L
                );

        AiKnowledgeSummaryResponse summaryResponse =
                createSummaryResponse();

        mockCommonContext(
                context
        );

        when(
                aiKnowledgeSummaryService.summarize(
                        conversationContent
                )
        ).thenReturn(
                summaryResponse
        );

        when(
                knowledgeNoteCommandService.saveIncremental(
                        user,
                        conversation,
                        summaryResponse
                )
        ).thenReturn(
                incrementalNote
        );

        when(
                knowledgeNoteRepository
                        .findFirstByUserIdAndConversationIdAndType(
                                USER_ID,
                                CONVERSATION_ID,
                                KnowledgeNoteType.CONSOLIDATED
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                knowledgeNoteCommandService.saveConsolidated(
                        user,
                        conversation,
                        summaryResponse
                )
        ).thenReturn(
                consolidatedNote
        );

        // when
        KnowledgeNote result =
                knowledgeExtractionService.extract(
                        USER_ID,
                        CONVERSATION_ID
                );

        // then
        assertThat(result)
                .isSameAs(consolidatedNote);

        InOrder inOrder =
                Mockito.inOrder(
                        userReader,
                        conversationReader,
                        contextBuilder,
                        aiKnowledgeSummaryService,
                        knowledgeNoteCommandService,
                        knowledgeNoteRepository,
                        conversation
                );

        verifyCommonStart(
                inOrder
        );

        inOrder.verify(aiKnowledgeSummaryService)
                .summarize(
                        conversationContent
                );

        inOrder.verify(knowledgeNoteCommandService)
                .saveIncremental(
                        user,
                        conversation,
                        summaryResponse
                );

        inOrder.verify(knowledgeNoteRepository)
                .findFirstByUserIdAndConversationIdAndType(
                        USER_ID,
                        CONVERSATION_ID,
                        KnowledgeNoteType.CONSOLIDATED
                );

        inOrder.verify(knowledgeNoteCommandService)
                .saveConsolidated(
                        user,
                        conversation,
                        summaryResponse
                );

        inOrder.verify(conversation)
                .completeKnowledgeExtraction(
                        11L
                );

        verify(aiKnowledgeMergeService, never())
                .merge(
                        consolidatedNote,
                        incrementalNote
                );

        verify(knowledgeNoteCommandService, never())
                .updateConsolidated(
                        user,
                        consolidatedNote,
                        null
                );
    }

    @Test
    void 통합_노트가_있으면_새_증분_노트를_생성하고_통합_노트를_병합한다() {
        // given
        String conversationContent = """
                USER:
                Tasklet과 Chunk 방식의 차이가 뭐야?
                
                AI:
                Tasklet은 단일 작업 중심이고 Chunk는 여러 Item을 묶어서 처리합니다.
                """.trim();

        ConversationAiContext context =
                createContext(
                        conversationContent,
                        20L,
                        21L
                );

        AiKnowledgeSummaryResponse summaryResponse =
                createSummaryResponse();

        AiKnowledgeMergeResponse mergeResponse =
                createMergeResponse();

        mockCommonContext(
                context
        );

        when(
                aiKnowledgeSummaryService.summarize(
                        conversationContent
                )
        ).thenReturn(
                summaryResponse
        );

        when(
                knowledgeNoteCommandService.saveIncremental(
                        user,
                        conversation,
                        summaryResponse
                )
        ).thenReturn(
                incrementalNote
        );

        when(
                knowledgeNoteRepository
                        .findFirstByUserIdAndConversationIdAndType(
                                USER_ID,
                                CONVERSATION_ID,
                                KnowledgeNoteType.CONSOLIDATED
                        )
        ).thenReturn(
                Optional.of(consolidatedNote)
        );

        when(
                aiKnowledgeMergeService.merge(
                        consolidatedNote,
                        incrementalNote
                )
        ).thenReturn(
                mergeResponse
        );

        when(
                knowledgeNoteCommandService.updateConsolidated(
                        user,
                        consolidatedNote,
                        mergeResponse
                )
        ).thenReturn(
                consolidatedNote
        );

        // when
        KnowledgeNote result =
                knowledgeExtractionService.extract(
                        USER_ID,
                        CONVERSATION_ID
                );

        // then
        assertThat(result)
                .isSameAs(consolidatedNote);

        InOrder inOrder =
                Mockito.inOrder(
                        userReader,
                        conversationReader,
                        contextBuilder,
                        aiKnowledgeSummaryService,
                        knowledgeNoteCommandService,
                        knowledgeNoteRepository,
                        aiKnowledgeMergeService,
                        conversation
                );

        verifyCommonStart(
                inOrder
        );

        inOrder.verify(aiKnowledgeSummaryService)
                .summarize(
                        conversationContent
                );

        inOrder.verify(knowledgeNoteCommandService)
                .saveIncremental(
                        user,
                        conversation,
                        summaryResponse
                );

        inOrder.verify(knowledgeNoteRepository)
                .findFirstByUserIdAndConversationIdAndType(
                        USER_ID,
                        CONVERSATION_ID,
                        KnowledgeNoteType.CONSOLIDATED
                );

        inOrder.verify(aiKnowledgeMergeService)
                .merge(
                        consolidatedNote,
                        incrementalNote
                );

        inOrder.verify(knowledgeNoteCommandService)
                .updateConsolidated(
                        user,
                        consolidatedNote,
                        mergeResponse
                );

        inOrder.verify(conversation)
                .completeKnowledgeExtraction(
                        21L
                );

        verify(knowledgeNoteCommandService, never())
                .saveConsolidated(
                        user,
                        conversation,
                        summaryResponse
                );
    }

    @Test
    void 통합_노트_병합이_실패하면_체크포인트를_갱신하지_않는다() {
        // given
        String conversationContent = """
                USER:
                Spring Batch 재시작은 어떻게 동작해?
                
                AI:
                JobRepository에 저장된 실행 상태를 기반으로 재시작할 수 있습니다.
                """.trim();

        ConversationAiContext context =
                createContext(
                        conversationContent,
                        30L,
                        31L
                );

        AiKnowledgeSummaryResponse summaryResponse =
                createSummaryResponse();

        mockCommonContext(
                context
        );

        when(
                aiKnowledgeSummaryService.summarize(
                        conversationContent
                )
        ).thenReturn(
                summaryResponse
        );

        when(
                knowledgeNoteCommandService.saveIncremental(
                        user,
                        conversation,
                        summaryResponse
                )
        ).thenReturn(
                incrementalNote
        );

        when(
                knowledgeNoteRepository
                        .findFirstByUserIdAndConversationIdAndType(
                                USER_ID,
                                CONVERSATION_ID,
                                KnowledgeNoteType.CONSOLIDATED
                        )
        ).thenReturn(
                Optional.of(consolidatedNote)
        );

        when(
                aiKnowledgeMergeService.merge(
                        consolidatedNote,
                        incrementalNote
                )
        ).thenThrow(
                new RuntimeException(
                        "AI 병합 실패"
                )
        );

        // when & then
        assertThatThrownBy(() ->
                knowledgeExtractionService.extract(
                        USER_ID,
                        CONVERSATION_ID
                )
        )
                .isInstanceOf(
                        RuntimeException.class
                )
                .hasMessage(
                        "AI 병합 실패"
                );

        verify(knowledgeNoteCommandService)
                .saveIncremental(
                        user,
                        conversation,
                        summaryResponse
                );

        verify(knowledgeNoteCommandService, never())
                .updateConsolidated(
                        user,
                        consolidatedNote,
                        null
                );

        verify(conversation, never())
                .completeKnowledgeExtraction(
                        31L
                );
    }

    private void mockCommonContext(
            ConversationAiContext context
    ) {
        when(
                userReader.get(
                        USER_ID
                )
        ).thenReturn(
                user
        );

        when(
                conversationReader.get(
                        USER_ID,
                        CONVERSATION_ID
                )
        ).thenReturn(
                conversation
        );

        when(
                contextBuilder.build(
                        conversation
                )
        ).thenReturn(
                context
        );
    }

    private void verifyCommonStart(
            InOrder inOrder
    ) {
        inOrder.verify(userReader)
                .get(
                        USER_ID
                );

        inOrder.verify(conversationReader)
                .get(
                        USER_ID,
                        CONVERSATION_ID
                );

        inOrder.verify(contextBuilder)
                .build(
                        conversation
                );
    }

    private ConversationAiContext createContext(
            String content,
            Long userMessageId,
            Long assistantMessageId
    ) {
        when(userMessage.getId())
                .thenReturn(
                        userMessageId
                );

        when(assistantMessage.getId())
                .thenReturn(
                        assistantMessageId
                );

        return new ConversationAiContext(
                List.of(
                        userMessage,
                        assistantMessage
                ),
                content
        );
    }

    private AiKnowledgeSummaryResponse createSummaryResponse() {
        return new AiKnowledgeSummaryResponse(
                KnowledgeRootCategory.WEB_DEVELOPMENT,
                List.of(
                        "Spring Framework",
                        "Spring Batch"
                ),
                "Spring Batch 기본 구조",
                "Job과 Step을 중심으로 Spring Batch 실행 구조를 정리한 노트",
                "Spring Batch는 Job과 Step을 중심으로 대용량 일괄 처리를 구성합니다.",
                List.of(
                        "Spring Batch",
                        "Job",
                        "Step"
                )
        );
    }

    private AiKnowledgeMergeResponse createMergeResponse() {
        return new AiKnowledgeMergeResponse(
                KnowledgeRootCategory.WEB_DEVELOPMENT,
                List.of(
                        "Spring Framework",
                        "Spring Batch"
                ),
                "Spring Batch 처리 방식",
                "Tasklet과 Chunk 처리 방식까지 반영한 통합 노트",
                "Spring Batch는 Job과 Step으로 구성되며 Tasklet 또는 Chunk 방식으로 작업을 처리할 수 있습니다.",
                List.of(
                        "Spring Batch",
                        "Tasklet",
                        "Chunk"
                )
        );
    }
}