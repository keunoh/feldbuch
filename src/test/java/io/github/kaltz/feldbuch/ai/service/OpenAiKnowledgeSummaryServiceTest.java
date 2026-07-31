package io.github.kaltz.feldbuch.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kaltz.feldbuch.ai.client.AiClient;
import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeSummaryResponse;
import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionResponse;
import io.github.kaltz.feldbuch.ai.dto.openai.Choice;
import io.github.kaltz.feldbuch.ai.dto.openai.Message;
import io.github.kaltz.feldbuch.common.exception.CustomException;
import io.github.kaltz.feldbuch.config.OpenAiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


class OpenAiKnowledgeSummaryServiceTest {

    private AiClient aiClient;

    private OpenAiProperties properties;

    private OpenAiKnowledgeSummaryService service;

    @BeforeEach
    void setUp() {

        aiClient = Mockito.mock(AiClient.class);
        properties = Mockito.mock(OpenAiProperties.class);

        service = new OpenAiKnowledgeSummaryService(
                aiClient,
                properties,
                new ObjectMapper()
        );
    }

    @Test
    void summarize() {

        when(properties.getModel())
                .thenReturn("gpt-4.1-mini");

        String json = """
                {
                  "knowledgePath": ["Backend", "Spring"],
                  "title": "JPA",
                  "description": "JPA 학습",
                  "summary": "JPA의 기본 개념을 학습하였다.",
                  "keywords": ["JPA", "Entity", "Repository"]
                }
                """;

        ChatCompletionResponse response =
                new ChatCompletionResponse(
                        List.of(
                                new Choice(
                                        new Message(
                                                "assistant",
                                                json
                                        )
                                )
                        )
                );

        when(aiClient.chat(any()))
                .thenReturn(response);

        AiKnowledgeSummaryResponse result =
                service.summarize("JPA를 공부했다.");

        assertThat(result.knowledgePath())
                .containsExactly("Backend", "Spring");

        assertThat(result.title())
                .isEqualTo("JPA");

        assertThat(result.description())
                .isEqualTo("JPA 학습");

        assertThat(result.summary())
                .isEqualTo("JPA의 기본 개념을 학습하였다.");

        assertThat(result.keywords())
                .containsExactly(
                        "JPA",
                        "Entity",
                        "Repository"
                );
    }

    @Test
    void summarize_응답이_비어있으면_예외를_발생시킨다() {

        when(properties.getModel())
                .thenReturn("gpt-4.1-mini");

        ChatCompletionResponse response =
                new ChatCompletionResponse(
                        List.of()
                );

        when(aiClient.chat(any()))
                .thenReturn(response);

        assertThatThrownBy(() ->
                service.summarize("Spring")
        ).isInstanceOf(CustomException.class);
    }
}