package io.github.kaltz.feldbuch.knowledge.service;

import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeSummaryResponse;
import io.github.kaltz.feldbuch.ai.service.AiKnowledgeSummaryService;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.reader.ConversationReader;
import io.github.kaltz.feldbuch.knowledge.context.ConversationKnowledgeContextBuilder;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeExtractionServiceTest {

    @Mock
    private UserReader userReader;

    @Mock
    private ConversationReader conversationReader;

    @Mock
    private ConversationKnowledgeContextBuilder contextBuilder;

    @Mock
    private AiKnowledgeSummaryService aiKnowledgeSummaryService;

    @Mock
    private KnowledgeNoteCommandService knowledgeNoteCommandService;

    @Mock
    private User user;

    @Mock
    private Conversation conversation;

    @Mock
    private KnowledgeNote knowledgeNote;

    @InjectMocks
    private KnowledgeExtractionService knowledgeExtractionService;

    @Test
    void 대화에서_지식을_추출하고_KnowledgeNote로_저장한다() {
        // given
        Long userId = 1L;
        Long conversationId = 1L;

        String conversationContext = """
                USER:
                Spring Batch가 무엇인지 설명해줘.
                
                AI:
                Spring Batch는 대용량 일괄 처리를 위한 프레임워크입니다.
                """.trim();

        AiKnowledgeSummaryResponse response =
                new AiKnowledgeSummaryResponse(
                        List.of(
                                "개발",
                                "Spring",
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

        when(userReader.get(userId))
                .thenReturn(user);

        when(conversationReader.get(userId, conversationId))
                .thenReturn(conversation);

        when(contextBuilder.build(userId, conversationId))
                .thenReturn(conversationContext);

        when(aiKnowledgeSummaryService.summarize(conversationContext))
                .thenReturn(response);

        when(
                knowledgeNoteCommandService.saveAiSummary(
                        user,
                        conversation,
                        response
                )
        ).thenReturn(knowledgeNote);

        // when
        KnowledgeNote result =
                knowledgeExtractionService.extract(
                        userId,
                        conversationId
                );

        // then
        assertThat(result).isSameAs(knowledgeNote);

        InOrder inOrder = Mockito.inOrder(
                userReader,
                conversationReader,
                contextBuilder,
                aiKnowledgeSummaryService,
                knowledgeNoteCommandService
        );

        inOrder.verify(userReader)
                .get(userId);

        inOrder.verify(conversationReader)
                .get(userId, conversationId);

        inOrder.verify(contextBuilder)
                .build(userId, conversationId);

        inOrder.verify(aiKnowledgeSummaryService)
                .summarize(conversationContext);

        inOrder.verify(knowledgeNoteCommandService)
                .saveAiSummary(
                        user,
                        conversation,
                        response
                );

        inOrder.verifyNoMoreInteractions();
    }

}