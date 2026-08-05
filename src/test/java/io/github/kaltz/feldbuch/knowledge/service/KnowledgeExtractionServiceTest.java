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
    private KnowledgeNote knowledgeNote;

    @Mock
    private KnowledgeNote existingNote;

    @InjectMocks
    private KnowledgeExtractionService knowledgeExtractionService;

    @Test
    void 기존_노트가_없으면_AI_요약으로_새_KnowledgeNote를_생성한다() {
        // given
        Long userId = 1L;
        Long conversationId = 1L;

        String conversationContent = """
                USER:
                Spring Batch가 무엇인지 설명해줘.
                
                AI:
                Spring Batch는 대용량 일괄 처리를 위한 프레임워크입니다.
                """.trim();

        ConversationAiContext context =
                new ConversationAiContext(
                        List.of(
                                userMessage,
                                assistantMessage
                        ),
                        conversationContent
                );

        AiKnowledgeSummaryResponse summaryResponse =
                new AiKnowledgeSummaryResponse(
                        KnowledgeRootCategory.WEB_DEVELOPMENT,
                        List.of(
                                "Spring Framework",
                                "Spring Batch"
                        ),
                        "Spring Batch 기본 구조",
                        "Job과 Step을 중심으로 Spring Batch의 실행 구조를 정리한 노트",
                        "Spring Batch는 대용량 일괄 처리를 지원하며 Job과 Step을 중심으로 작업을 구성합니다.",
                        List.of(
                                "Spring Batch",
                                "Job",
                                "Step"
                        )
                );

        when(userMessage.getId())
                .thenReturn(10L);

        when(assistantMessage.getId())
                .thenReturn(11L);

        when(userReader.get(userId))
                .thenReturn(user);

        when(
                conversationReader.get(
                        userId,
                        conversationId
                )
        ).thenReturn(conversation);

        when(contextBuilder.build(conversation))
                .thenReturn(context);

        when(
                knowledgeNoteRepository
                        .findFirstByUserIdAndConversationIdOrderByCreatedAtAsc(
                                userId,
                                conversationId
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                aiKnowledgeSummaryService.summarize(
                        conversationContent
                )
        ).thenReturn(
                summaryResponse
        );

        when(
                knowledgeNoteCommandService.saveAiSummary(
                        user,
                        conversation,
                        summaryResponse
                )
        ).thenReturn(
                knowledgeNote
        );

        // when
        KnowledgeNote result =
                knowledgeExtractionService.extract(
                        userId,
                        conversationId
                );

        // then
        assertThat(result)
                .isSameAs(knowledgeNote);

        InOrder inOrder =
                Mockito.inOrder(
                        userReader,
                        conversationReader,
                        contextBuilder,
                        knowledgeNoteRepository,
                        aiKnowledgeSummaryService,
                        knowledgeNoteCommandService,
                        conversation
                );

        inOrder.verify(userReader)
                .get(userId);

        inOrder.verify(conversationReader)
                .get(
                        userId,
                        conversationId
                );

        inOrder.verify(contextBuilder)
                .build(conversation);

        inOrder.verify(knowledgeNoteRepository)
                .findFirstByUserIdAndConversationIdOrderByCreatedAtAsc(
                        userId,
                        conversationId
                );

        inOrder.verify(aiKnowledgeSummaryService)
                .summarize(
                        conversationContent
                );

        inOrder.verify(knowledgeNoteCommandService)
                .saveAiSummary(
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
                        existingNote,
                        conversationContent
                );

        verify(knowledgeNoteCommandService, never())
                .updateAiSummary(
                        user,
                        existingNote,
                        null
                );
    }

    @Test
    void 기존_노트가_있으면_AI_병합으로_기존_KnowledgeNote를_갱신한다() {
        // given
        Long userId = 1L;
        Long conversationId = 1L;

        String conversationContent = """
                USER:
                Tasklet과 Chunk 방식의 차이가 뭐야?
                
                AI:
                Tasklet은 단일 작업 중심이고 Chunk는 여러 Item을 묶어서 처리합니다.
                """.trim();

        ConversationAiContext context =
                new ConversationAiContext(
                        List.of(
                                userMessage,
                                assistantMessage
                        ),
                        conversationContent
                );

        AiKnowledgeMergeResponse mergeResponse =
                new AiKnowledgeMergeResponse(
                        KnowledgeRootCategory.WEB_DEVELOPMENT,
                        List.of(
                                "Spring Framework",
                                "Spring Batch"
                        ),
                        "Spring Batch 처리 방식",
                        "Tasklet과 Chunk 처리 방식까지 포함해 Spring Batch 구조를 정리한 노트",
                        "Spring Batch는 Job과 Step으로 구성되며 Step에서는 Tasklet 또는 Chunk 기반 처리를 사용할 수 있습니다.",
                        List.of(
                                "Spring Batch",
                                "Tasklet",
                                "Chunk"
                        )
                );

        when(userMessage.getId())
                .thenReturn(20L);

        when(assistantMessage.getId())
                .thenReturn(21L);

        when(userReader.get(userId))
                .thenReturn(user);

        when(
                conversationReader.get(
                        userId,
                        conversationId
                )
        ).thenReturn(conversation);

        when(contextBuilder.build(conversation))
                .thenReturn(context);

        when(
                knowledgeNoteRepository
                        .findFirstByUserIdAndConversationIdOrderByCreatedAtAsc(
                                userId,
                                conversationId
                        )
        ).thenReturn(
                Optional.of(existingNote)
        );

        when(
                aiKnowledgeMergeService.merge(
                        existingNote,
                        conversationContent
                )
        ).thenReturn(
                mergeResponse
        );

        when(
                knowledgeNoteCommandService.updateAiSummary(
                        user,
                        existingNote,
                        mergeResponse
                )
        ).thenReturn(
                existingNote
        );

        // when
        KnowledgeNote result =
                knowledgeExtractionService.extract(
                        userId,
                        conversationId
                );

        // then
        assertThat(result)
                .isSameAs(existingNote);

        InOrder inOrder =
                Mockito.inOrder(
                        userReader,
                        conversationReader,
                        contextBuilder,
                        knowledgeNoteRepository,
                        aiKnowledgeMergeService,
                        knowledgeNoteCommandService,
                        conversation
                );

        inOrder.verify(userReader)
                .get(userId);

        inOrder.verify(conversationReader)
                .get(
                        userId,
                        conversationId
                );

        inOrder.verify(contextBuilder)
                .build(conversation);

        inOrder.verify(knowledgeNoteRepository)
                .findFirstByUserIdAndConversationIdOrderByCreatedAtAsc(
                        userId,
                        conversationId
                );

        inOrder.verify(aiKnowledgeMergeService)
                .merge(
                        existingNote,
                        conversationContent
                );

        inOrder.verify(knowledgeNoteCommandService)
                .updateAiSummary(
                        user,
                        existingNote,
                        mergeResponse
                );

        inOrder.verify(conversation)
                .completeKnowledgeExtraction(
                        21L
                );

        verify(aiKnowledgeSummaryService, never())
                .summarize(
                        conversationContent
                );

        verify(knowledgeNoteCommandService, never())
                .saveAiSummary(
                        user,
                        conversation,
                        null
                );
    }

    @Test
    void AI_병합이_실패하면_추출_체크포인트를_갱신하지_않는다() {
        // given
        Long userId = 1L;
        Long conversationId = 1L;

        String conversationContent = """
                USER:
                Spring Batch 재시작은 어떻게 동작해?
                
                AI:
                JobRepository에 저장된 실행 상태를 기반으로 재시작할 수 있습니다.
                """.trim();

        ConversationAiContext context =
                new ConversationAiContext(
                        List.of(
                                userMessage,
                                assistantMessage
                        ),
                        conversationContent
                );

        when(userMessage.getId())
                .thenReturn(30L);

        when(assistantMessage.getId())
                .thenReturn(31L);

        when(userReader.get(userId))
                .thenReturn(user);

        when(
                conversationReader.get(
                        userId,
                        conversationId
                )
        ).thenReturn(conversation);

        when(contextBuilder.build(conversation))
                .thenReturn(context);

        when(
                knowledgeNoteRepository
                        .findFirstByUserIdAndConversationIdOrderByCreatedAtAsc(
                                userId,
                                conversationId
                        )
        ).thenReturn(
                Optional.of(existingNote)
        );

        when(
                aiKnowledgeMergeService.merge(
                        existingNote,
                        conversationContent
                )
        ).thenThrow(
                new RuntimeException(
                        "AI 병합 실패"
                )
        );

        // when & then
        assertThatThrownBy(() ->
                knowledgeExtractionService.extract(
                        userId,
                        conversationId
                )
        )
                .isInstanceOf(
                        RuntimeException.class
                )
                .hasMessage(
                        "AI 병합 실패"
                );

        verify(conversation, never())
                .completeKnowledgeExtraction(
                        31L
                );

        verify(knowledgeNoteCommandService, never())
                .updateAiSummary(
                        user,
                        existingNote,
                        null
                );
    }
}