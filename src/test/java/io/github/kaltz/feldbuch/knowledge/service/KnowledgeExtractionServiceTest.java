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
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeCategory;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNoteType;
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

    private static final Long CONVERSATION_ID = 10L;

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
    void 통합_노트가_없으면_증분_노트와_통합_노트를_생성한다() {
        // given
        String conversationContent = """
                USER:
                Spring Batch의 Job과 Step 구조를 설명해줘.
                
                AI:
                Job은 전체 배치 작업이고 Step은 실제 처리 단위입니다.
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
    void 통합_노트가_있으면_증분_노트를_생성하고_통합_노트를_병합한다() {
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
    void 병합_결과의_카테고리가_달라도_CommandService에_그대로_전달한다() {
        // given
        String conversationContent = """
                USER:
                이번에는 JPA 영속성 컨텍스트를 설명해줘.
                
                AI:
                영속성 컨텍스트는 Entity를 관리하는 논리적 공간입니다.
                """.trim();

        ConversationAiContext context =
                createContext(
                        conversationContent,
                        30L,
                        31L
                );

        AiKnowledgeSummaryResponse summaryResponse =
                new AiKnowledgeSummaryResponse(
                        KnowledgeCategory.JPA,
                        "영속성 컨텍스트의 역할",
                        "JPA가 Entity를 관리하는 공간을 정리한 노트",
                        "영속성 컨텍스트는 Entity의 상태를 관리하고 1차 캐시와 변경 감지를 제공합니다.",
                        List.of(
                                "JPA",
                                "영속성 컨텍스트",
                                "1차 캐시"
                        )
                );

        AiKnowledgeMergeResponse mergeResponse =
                new AiKnowledgeMergeResponse(
                        KnowledgeCategory.JPA,
                        "영속성 컨텍스트와 변경 감지",
                        "Entity 관리와 변경 반영 과정을 정리한 통합 노트",
                        "JPA는 영속성 컨텍스트를 통해 Entity를 관리하고 flush 시 변경 사항을 반영합니다.",
                        List.of(
                                "JPA",
                                "영속성 컨텍스트",
                                "flush",
                                "변경 감지"
                        )
                );

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

        verify(knowledgeNoteCommandService)
                .updateConsolidated(
                        user,
                        consolidatedNote,
                        mergeResponse
                );

        verify(conversation)
                .completeKnowledgeExtraction(
                        31L
                );
    }

    @Test
    void AI_요약이_실패하면_노트를_저장하거나_체크포인트를_갱신하지_않는다() {
        // given
        String conversationContent = """
                USER:
                Spring Batch를 설명해줘.
                
                AI:
                Spring Batch는 배치 처리 프레임워크입니다.
                """.trim();

        ConversationAiContext context =
                createContext(
                        conversationContent,
                        40L,
                        41L
                );

        mockCommonContext(
                context
        );

        when(
                aiKnowledgeSummaryService.summarize(
                        conversationContent
                )
        ).thenThrow(
                new RuntimeException(
                        "AI 요약 실패"
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
                        "AI 요약 실패"
                );

        verify(knowledgeNoteCommandService, never())
                .saveIncremental(
                        user,
                        conversation,
                        createSummaryResponse()
                );

        verify(conversation, never())
                .completeKnowledgeExtraction(
                        41L
                );
    }

    @Test
    void AI_병합이_실패하면_체크포인트를_갱신하지_않는다() {
        // given
        String conversationContent = """
                USER:
                Spring Batch의 Retry 정책을 알려줘.
                
                AI:
                Retry는 일시적인 실패가 발생했을 때 작업을 다시 시도하는 정책입니다.
                """.trim();

        ConversationAiContext context =
                createContext(
                        conversationContent,
                        50L,
                        51L
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
                        51L
                );
    }

    @Test
    void AI_컨텍스트가_비어있으면_추출에_실패한다() {
        // given
        ConversationAiContext emptyContext =
                new ConversationAiContext(
                        List.of(),
                        ""
                );

        mockCommonContext(
                emptyContext
        );

        // when & then
        assertThatThrownBy(() ->
                knowledgeExtractionService.extract(
                        USER_ID,
                        CONVERSATION_ID
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "지식으로 추출할 새로운 대화 메시지가 없습니다."
                );

        verify(aiKnowledgeSummaryService, never())
                .summarize(
                        ""
                );

        verify(conversation, never())
                .completeKnowledgeExtraction(
                        null
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
                KnowledgeCategory.SPRING_BATCH,
                "Spring Batch 기본 구조",
                "Job과 Step 중심의 실행 구조를 정리한 노트",
                "Spring Batch는 Job과 Step을 중심으로 배치 작업을 구성합니다.",
                List.of(
                        "Spring Batch",
                        "Job",
                        "Step"
                )
        );
    }

    private AiKnowledgeMergeResponse createMergeResponse() {
        return new AiKnowledgeMergeResponse(
                KnowledgeCategory.SPRING_BATCH,
                "Spring Batch 실행 구조와 처리 방식",
                "Job, Step, Tasklet과 Chunk 구조를 정리한 통합 노트",
                "Spring Batch는 Job과 Step으로 구성되며 Step은 Tasklet 또는 Chunk 방식으로 작업을 처리할 수 있습니다.",
                List.of(
                        "Spring Batch",
                        "Job",
                        "Step",
                        "Tasklet",
                        "Chunk"
                )
        );
    }
}