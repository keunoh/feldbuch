package io.github.kaltz.feldbuch.batch.tasklet;

import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.reader.KnowledgeConversationReader;
import io.github.kaltz.feldbuch.knowledge.service.KnowledgeExtractionService;
import io.github.kaltz.feldbuch.knowledge.service.KnowledgeExtractionStatusService;
import io.github.kaltz.feldbuch.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnowledgeExtractionTaskletTest {

    @Mock
    private KnowledgeConversationReader conversationReader;

    @Mock
    private KnowledgeExtractionService extractionService;

    @Mock
    private KnowledgeExtractionStatusService statusService;

    @Mock
    private StepContribution contribution;

    @Mock
    private ChunkContext chunkContext;

    @Mock
    private Conversation conversation;

    @Mock
    private User user;

    @Mock
    private Conversation failedConversation;

    @Mock
    private Conversation successConversation;

    @Mock
    private User failedUser;

    @Mock
    private User successUser;

    @InjectMocks
    private KnowledgeExtractionTasklet tasklet;

    @Test
    void 지식_추출_배치를_실행한다() throws Exception {

        // given
        when(conversationReader.findExtractionTargets())
                .thenReturn(List.of(conversation));

        when(conversation.getId())
                .thenReturn(1L);

        when(conversation.getUser())
                .thenReturn(user);

        when(user.getId())
                .thenReturn(100L);

        // when
        RepeatStatus status =
                tasklet.execute(
                        contribution,
                        chunkContext
                );

        // then
        assertThat(status)
                .isEqualTo(RepeatStatus.FINISHED);

        InOrder inOrder =
                inOrder(
                        conversationReader,
                        statusService,
                        extractionService
                );

        inOrder.verify(conversationReader)
                .findExtractionTargets();

        inOrder.verify(statusService)
                .start(1L);

        inOrder.verify(extractionService)
                .extract(
                        100L,
                        1L
                );

        inOrder.verify(statusService)
                .complete(1L);

        verify(statusService, never())
                .fail(
                        anyLong(),
                        eq("AI 호출 실패")
                );
    }

    @Test
    void 한_대화의_지식_추출이_실패해도_다음_대화를_계속_처리한다() throws Exception {

        // given
        Long failedConversationId = 1L;
        Long failedUserId = 100L;

        Long successConversationId = 2L;
        Long successUserId = 200L;

        when(conversationReader.findExtractionTargets())
                .thenReturn(
                        List.of(
                                failedConversation,
                                successConversation
                        )
                );

        when(failedConversation.getId())
                .thenReturn(failedConversationId);

        when(failedConversation.getUser())
                .thenReturn(failedUser);

        when(failedUser.getId())
                .thenReturn(failedUserId);

        when(successConversation.getId())
                .thenReturn(successConversationId);

        when(successConversation.getUser())
                .thenReturn(successUser);

        when(successUser.getId())
                .thenReturn(successUserId);

        // 아래 설정이 첫 번째 대화의 AI 지식 추출 실패를 흉내 낸다.
        doThrow(new RuntimeException("AI 호출 실패"))
                .when(extractionService)
                .extract(
                        failedUserId,
                        failedConversationId
                );

        // when
        RepeatStatus status =
                tasklet.execute(
                        contribution,
                        chunkContext
                );

        // then
        assertThat(status)
                .isEqualTo(RepeatStatus.FINISHED);

        InOrder inOrder = inOrder(
                conversationReader,
                statusService,
                extractionService
        );

        inOrder.verify(conversationReader)
                .findExtractionTargets();

        // 첫 번째 대화
        inOrder.verify(statusService)
                .start(failedConversationId);

        inOrder.verify(extractionService)
                .extract(
                        failedUserId,
                        failedConversationId
                );

        inOrder.verify(statusService)
                .fail(
                        failedConversationId,
                        "AI 호출 실패"
                );

        // 두 번째 대화
        inOrder.verify(statusService)
                .start(successConversationId);

        inOrder.verify(extractionService)
                .extract(
                        successUserId,
                        successConversationId
                );

        inOrder.verify(statusService)
                .complete(successConversationId);

        verify(statusService, never())
                .complete(failedConversationId);

        verify(statusService, never())
                .fail(
                        successConversationId,
                        "AI 호출 실패"
                );
    }

}