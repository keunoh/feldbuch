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
        aiClient =
                Mockito.mock(
                        AiClient.class
                );

        properties =
                Mockito.mock(
                        OpenAiProperties.class
                );

        service =
                new OpenAiKnowledgeSummaryService(
                        aiClient,
                        properties,
                        new ObjectMapper()
                );
    }

    @Test
    void 지식_요약_응답을_변환한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        String json = """
                {
                  "knowledgePath": [
                    "JPA",
                    "영속성 관리"
                  ],
                  "title": "JPA 기본 개념",
                  "description": "JPA의 핵심 개념과 영속성 관리 방식을 정리한 노트",
                  "summary": "JPA는 자바 객체와 관계형 데이터베이스 사이의 매핑을 지원하며 영속성 컨텍스트를 통해 엔티티를 관리합니다.",
                  "keywords": [
                    "JPA",
                    "Entity",
                    "Repository"
                  ]
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

        // when
        AiKnowledgeSummaryResponse result =
                service.summarize("JPA를 공부했다.");

        // then
        assertThat(result.knowledgePath())
                .containsExactly(
                        "JPA",
                        "영속성 관리"
                );

        assertThat(result.title())
                .isEqualTo(
                        "JPA 기본 개념"
                );

        assertThat(result.description())
                .isEqualTo(
                        "JPA의 핵심 개념과 영속성 관리 방식을 정리한 노트"
                );

        assertThat(result.summary())
                .isEqualTo(
                        "JPA는 자바 객체와 관계형 데이터베이스 사이의 매핑을 지원하며 영속성 컨텍스트를 통해 엔티티를 관리합니다."
                );

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

        // when & then
        assertThatThrownBy(() ->
                service.summarize("Spring")
        ).isInstanceOf(CustomException.class);
    }


    @Test
    void 하위_지식_경로가_2단계를_초과하면_예외가_발생한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        String json = """
                {
                  "knowledgePath": [
                    "Spring",
                    "WebFlux",
                    "Reactive Streams"
                  ],
                  "title": "Spring WebFlux",
                  "description": "Spring WebFlux 학습 노트",
                  "summary": "Spring WebFlux의 비동기 처리 방식을 정리했습니다.",
                  "keywords": [
                    "Spring",
                    "WebFlux",
                    "Reactive"
                  ]
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

        // when & then
        assertThatThrownBy(() ->
                service.summarize("WebFlux를 공부했다."))
                .isInstanceOf(CustomException.class);
    }

    
}