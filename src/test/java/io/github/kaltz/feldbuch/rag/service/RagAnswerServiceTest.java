package io.github.kaltz.feldbuch.rag.service;

import io.github.kaltz.feldbuch.ai.model.ChatCommand;
import io.github.kaltz.feldbuch.ai.model.ChatMessage;
import io.github.kaltz.feldbuch.ai.model.ChatResponse;
import io.github.kaltz.feldbuch.ai.model.ChatRole;
import io.github.kaltz.feldbuch.ai.service.ChatService;
import io.github.kaltz.feldbuch.rag.context.KnowledgeContextBuilder;
import io.github.kaltz.feldbuch.rag.model.RagAnswerResult;
import io.github.kaltz.feldbuch.rag.prompt.RagPromptFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RagAnswerServiceTest {

    @Mock
    private KnowledgeSearchService knowledgeSearchService;

    @Mock
    private KnowledgeContextBuilder knowledgeContextBuilder;

    @Mock
    private RagPromptFactory ragPromptFactory;

    @Mock
    private ChatService chatService;

    @InjectMocks
    private RagAnswerService ragAnswerService;

    @Test
    void 검색된_지식을_기반으로_AI_답변을_생성한다() {

        // given
        Long userId = 1L;
        String question = "Spring 트랜잭션은 어떻게 사용해?";

        Document document =
                new Document(
                        "test-document-id",
                        "Spring에서는 @Transactional을 사용합니다.",
                        Map.of(
                                "knowledgeNoteId", 10L,
                                "knowledgeId", 20L,
                                "conversationId", 30L
                        )
                );

        List<Document> documents = List.of(document);

        String context =
                """
                        [지식 1]
                        Spring에서는 @Transactional을 사용합니다.
                        """;

        List<ChatMessage> messages =
                List.of(
                        new ChatMessage(
                                ChatRole.SYSTEM,
                                "system prompt"
                        ),
                        new ChatMessage(
                                ChatRole.USER,
                                "user prompt"
                        )
                );

        ChatResponse expectedResponse =
                new ChatResponse(
                        "Spring에서는 @Transactional을 사용하여 트랜잭션을 관리합니다."
                );

        when(
                knowledgeSearchService.search(
                        userId,
                        question
                )
        )
                .thenReturn(documents);

        when(
                knowledgeContextBuilder.build(
                        documents
                )
        )
                .thenReturn(context);

        when(
                ragPromptFactory.create(
                        question,
                        context
                )
        )
                .thenReturn(messages);

        when(
                chatService.chat(
                        any(ChatCommand.class)
                )
        )
                .thenReturn(expectedResponse);

        // when
        RagAnswerResult result =
                ragAnswerService.answer(
                        userId,
                        question
                );

        // then
        assertThat(result.response())
                .isEqualTo(expectedResponse);

        assertThat(result.sources())
                .hasSize(1);

        assertThat(result.sources().get(0).knowledgeNoteId())
                .isEqualTo(10L);

        assertThat(result.sources().get(0).knowledgeId())
                .isEqualTo(20L);

        assertThat(result.sources().get(0).conversationId())
                .isEqualTo(30L);

        verify(knowledgeSearchService)
                .search(
                        userId,
                        question
                );

        verify(knowledgeContextBuilder)
                .build(
                        documents
                );

        verify(ragPromptFactory)
                .create(
                        question,
                        context
                );

        verify(chatService)
                .chat(
                        any(ChatCommand.class)
                );
    }

    @Test
    void 관련_지식이_없으면_일반_AI_답변을_생성한다() {

        // given
        Long userId = 1L;
        String question =
                "오늘 점심 뭐 먹을까?";

        ChatResponse expectedResponse =
                new ChatResponse(
                        "가볍게 먹고 싶다면 샌드위치는 어떠세요?"
                );

        when(
                knowledgeSearchService.search(userId, question)
        )
                .thenReturn(List.of());

        when(
                chatService.chat(any(ChatCommand.class))
        )
                .thenReturn(expectedResponse);

        // when
        RagAnswerResult result =
                ragAnswerService.answer(userId, question);

        // then
        assertThat(result.response())
                .isEqualTo(expectedResponse);

        assertThat(result.sources())
                .isEmpty();

        verify(knowledgeSearchService)
                .search(userId, question);

        verifyNoInteractions(
                knowledgeContextBuilder,
                ragPromptFactory
        );

        verify(chatService)
                .chat(any(ChatCommand.class));
    }
}