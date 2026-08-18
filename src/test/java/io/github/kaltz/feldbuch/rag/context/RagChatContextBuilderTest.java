package io.github.kaltz.feldbuch.rag.context;

import io.github.kaltz.feldbuch.ai.context.ChatContextBuilder;
import io.github.kaltz.feldbuch.ai.model.ChatCommand;
import io.github.kaltz.feldbuch.ai.model.ChatMessage;
import io.github.kaltz.feldbuch.ai.model.ChatRole;
import io.github.kaltz.feldbuch.rag.service.KnowledgeSearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RagChatContextBuilderTest {

    @Mock
    private ChatContextBuilder chatContextBuilder;

    @Mock
    private KnowledgeSearchService knowledgeSearchService;

    @Mock
    private KnowledgeContextBuilder knowledgeContextBuilder;

    @InjectMocks
    private RagChatContextBuilder ragChatContextBuilder;

    @Test
    void 관련_지식이_있으면_기존_대화에_지식_Context를_추가한다() {

        // given
        Long userId = 1L;
        Long conversationId = 10L;
        String question = "Spring 트랜잭션은 어떻게 사용해?";

        ChatCommand conversationCommand =
                ChatCommand.from(
                        List.of(
                                new ChatMessage(
                                        ChatRole.USER,
                                        "Spring을 공부하고 있어."
                                ),
                                new ChatMessage(
                                        ChatRole.ASSISTANT,
                                        "어떤 내용을 공부하고 있나요?"
                                ),
                                new ChatMessage(
                                        ChatRole.USER,
                                        question
                                )
                        )
                );

        List<Document> documents =
                List.of(
                        new Document(
                                "Spring에서는 @Transactional을 사용합니다."
                        )
                );

        String knowledgeContext =
                """
                        [지식 1]
                        Spring에서는 @Transactional을 사용합니다.
                        """;

        when(
                chatContextBuilder.build(
                        userId,
                        conversationId
                )
        )
                .thenReturn(
                        conversationCommand
                );

        when(
                knowledgeSearchService.search(
                        userId,
                        question
                )
        )
                .thenReturn(
                        documents
                );

        when(
                knowledgeContextBuilder.build(
                        documents
                )
        )
                .thenReturn(
                        knowledgeContext
                );

        // when
        ChatCommand result =
                ragChatContextBuilder.build(
                        userId,
                        conversationId,
                        question
                );

        // then
        assertThat(result.messages())
                .hasSize(
                        conversationCommand.messages().size() + 1
                );

        ChatMessage knowledgeMessage =
                result.messages().get(0);

        assertThat(knowledgeMessage.role())
                .isEqualTo(
                        ChatRole.SYSTEM
                );

        assertThat(knowledgeMessage.content())
                .contains(
                        "사용자가 이전에 정리한 개인 지식"
                )
                .contains(
                        "@Transactional"
                );

        assertThat(
                result.messages()
                        .subList(
                                1,
                                result.messages().size()
                        )
        )
                .containsExactlyElementsOf(
                        conversationCommand.messages()
                );

        verify(chatContextBuilder)
                .build(
                        userId,
                        conversationId
                );

        verify(knowledgeSearchService)
                .search(
                        userId,
                        question
                );

        verify(knowledgeContextBuilder)
                .build(
                        documents
                );
    }

    @Test
    void 관련_지식이_없으면_기존_대화_Context를_그대로_사용한다() {

        // given
        Long userId = 1L;
        Long conversationId = 10L;
        String question = "오늘 점심 뭐 먹을까?";

        ChatCommand conversationCommand =
                ChatCommand.from(
                        List.of(
                                new ChatMessage(
                                        ChatRole.USER,
                                        question
                                )
                        )
                );

        when(
                chatContextBuilder.build(
                        userId,
                        conversationId
                )
        )
                .thenReturn(
                        conversationCommand
                );

        when(
                knowledgeSearchService.search(
                        userId,
                        question
                )
        )
                .thenReturn(
                        List.of()
                );

        // when
        ChatCommand result =
                ragChatContextBuilder.build(
                        userId,
                        conversationId,
                        question
                );

        // then
        assertThat(result)
                .isSameAs(
                        conversationCommand
                );

        verify(chatContextBuilder)
                .build(
                        userId,
                        conversationId
                );

        verify(knowledgeSearchService)
                .search(
                        userId,
                        question
                );

        verifyNoInteractions(
                knowledgeContextBuilder
        );
    }
}