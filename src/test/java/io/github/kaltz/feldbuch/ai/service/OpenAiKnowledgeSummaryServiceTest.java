package io.github.kaltz.feldbuch.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kaltz.feldbuch.ai.client.AiClient;
import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeSummaryResponse;
import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionRequest;
import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionResponse;
import io.github.kaltz.feldbuch.ai.dto.openai.Choice;
import io.github.kaltz.feldbuch.ai.dto.openai.Message;
import io.github.kaltz.feldbuch.common.exception.CustomException;
import io.github.kaltz.feldbuch.config.OpenAiProperties;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
    void 대화를_AI_학습_노트로_요약한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": "Job과 Step의 역할",
                  "description": "Spring Batch의 실행 단위를 정리한 노트",
                  "summary": "Job은 전체 배치 작업을 나타내며 Step은 실제 처리 단위를 담당합니다.",
                  "keywords": [
                    "Spring Batch",
                    "Job",
                    "Step"
                  ]
                }
                """;

        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
                responseOf(json)
        );

        // when
        AiKnowledgeSummaryResponse result =
                service.summarize(
                        "Spring Batch에서 Job과 Step의 역할을 설명해줘."
                );

        // then
        assertThat(result.category())
                .isEqualTo(
                        KnowledgeCategory.SPRING_BATCH
                );

        assertThat(result.title())
                .isEqualTo(
                        "Job과 Step의 역할"
                );

        assertThat(result.description())
                .isEqualTo(
                        "Spring Batch의 실행 단위를 정리한 노트"
                );

        assertThat(result.summary())
                .isEqualTo(
                        "Job은 전체 배치 작업을 나타내며 Step은 실제 처리 단위를 담당합니다."
                );

        assertThat(result.keywords())
                .containsExactly(
                        "Spring Batch",
                        "Job",
                        "Step"
                );
    }

    @Test
    void AI_요청에_시스템_프롬프트와_대화_내용을_포함한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        String conversation =
                "JPA 영속성 컨텍스트의 역할을 설명해줘.";

        String json = """
                {
                  "category": "JPA",
                  "title": "영속성 컨텍스트의 역할",
                  "description": "JPA의 Entity 관리 공간을 정리한 노트",
                  "summary": "영속성 컨텍스트는 Entity의 상태를 관리합니다.",
                  "keywords": [
                    "JPA",
                    "영속성 컨텍스트",
                    "Entity"
                  ]
                }
                """;

        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
                responseOf(json)
        );

        ArgumentCaptor<ChatCompletionRequest> captor =
                ArgumentCaptor.forClass(
                        ChatCompletionRequest.class
                );

        // when
        service.summarize(
                conversation
        );

        // then
        verify(aiClient)
                .chat(
                        captor.capture()
                );

        ChatCompletionRequest request =
                captor.getValue();

        assertThat(request.model())
                .isEqualTo(
                        "gpt-4.1-mini"
                );

        assertThat(request.messages())
                .hasSize(2);

        Message systemMessage =
                request.messages()
                        .getFirst();

        Message userMessage =
                request.messages()
                        .get(1);

        assertThat(systemMessage.role())
                .isEqualTo(
                        "system"
                );

        assertThat(systemMessage.content())
                .contains(
                        "\"category\"",
                        "SPRING_BATCH",
                        "JPA",
                        "MYSQL",
                        "DOCKER"
                );

        assertThat(userMessage.role())
                .isEqualTo(
                        "user"
                );

        assertThat(userMessage.content())
                .contains(
                        conversation
                );
    }

    @Test
    void AI_응답의_카테고리를_enum으로_변환한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        String json = """
                {
                  "category": "JPA",
                  "title": "영속성 컨텍스트",
                  "description": "JPA의 Entity 관리 구조",
                  "summary": "영속성 컨텍스트는 Entity를 관리합니다.",
                  "keywords": [
                    "JPA",
                    "Entity",
                    "영속성 컨텍스트"
                  ]
                }
                """;

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(json)
                );

        // when
        AiKnowledgeSummaryResponse result =
                service.summarize(
                        "영속성 컨텍스트란?"
                );

        // then
        assertThat(result.category())
                .isSameAs(
                        KnowledgeCategory.JPA
                );
    }

    @Test
    void 지원하지_않는_카테고리가_반환되면_예외가_발생한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        String json = """
                {
                  "category": "SPRING_BATCH_FRAMEWORK",
                  "title": "Spring Batch",
                  "description": "설명",
                  "summary": "요약",
                  "keywords": [
                    "Spring Batch",
                    "Job",
                    "Step"
                  ]
                }
                """;

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(json)
                );

        // when & then
        assertThatThrownBy(() ->
                service.summarize(
                        "Spring Batch를 설명해줘."
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_응답의_카테고리가_null이면_예외가_발생한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        String json = """
                {
                  "category": null,
                  "title": "Spring Batch",
                  "description": "설명",
                  "summary": "요약",
                  "keywords": [
                    "Spring Batch",
                    "Job",
                    "Step"
                  ]
                }
                """;

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(json)
                );

        // when & then
        assertThatThrownBy(() ->
                service.summarize(
                        "Spring Batch를 설명해줘."
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_응답의_제목이_비어있으면_예외가_발생한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": " ",
                  "description": "Spring Batch 설명",
                  "summary": "Spring Batch 요약",
                  "keywords": [
                    "Spring Batch",
                    "Job",
                    "Step"
                  ]
                }
                """;

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(json)
                );

        // when & then
        assertThatThrownBy(() ->
                service.summarize(
                        "Spring Batch"
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_응답의_설명이_비어있으면_예외가_발생한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": "Spring Batch",
                  "description": "",
                  "summary": "Spring Batch 요약",
                  "keywords": [
                    "Spring Batch",
                    "Job",
                    "Step"
                  ]
                }
                """;

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(json)
                );

        // when & then
        assertThatThrownBy(() ->
                service.summarize(
                        "Spring Batch"
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_응답의_요약이_비어있으면_예외가_발생한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": "Spring Batch",
                  "description": "Spring Batch 설명",
                  "summary": null,
                  "keywords": [
                    "Spring Batch",
                    "Job",
                    "Step"
                  ]
                }
                """;

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(json)
                );

        // when & then
        assertThatThrownBy(() ->
                service.summarize(
                        "Spring Batch"
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_응답의_키워드가_3개_미만이면_예외가_발생한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": "Spring Batch",
                  "description": "Spring Batch 설명",
                  "summary": "Spring Batch 요약",
                  "keywords": [
                    "Spring Batch",
                    "Job"
                  ]
                }
                """;

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(json)
                );

        // when & then
        assertThatThrownBy(() ->
                service.summarize(
                        "Spring Batch"
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_응답의_키워드가_7개를_초과하면_예외가_발생한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": "Spring Batch",
                  "description": "Spring Batch 설명",
                  "summary": "Spring Batch 요약",
                  "keywords": [
                    "Spring Batch",
                    "Job",
                    "Step",
                    "Tasklet",
                    "Chunk",
                    "Reader",
                    "Processor",
                    "Writer"
                  ]
                }
                """;

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(json)
                );

        // when & then
        assertThatThrownBy(() ->
                service.summarize(
                        "Spring Batch"
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_응답의_키워드에_빈_값이_있으면_예외가_발생한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": "Spring Batch",
                  "description": "Spring Batch 설명",
                  "summary": "Spring Batch 요약",
                  "keywords": [
                    "Spring Batch",
                    " ",
                    "Step"
                  ]
                }
                """;

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(json)
                );

        // when & then
        assertThatThrownBy(() ->
                service.summarize(
                        "Spring Batch"
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_응답의_키워드가_중복되면_예외가_발생한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": "Spring Batch",
                  "description": "Spring Batch 설명",
                  "summary": "Spring Batch 요약",
                  "keywords": [
                    "Spring Batch",
                    "Job",
                    "Job"
                  ]
                }
                """;

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(json)
                );

        // when & then
        assertThatThrownBy(() ->
                service.summarize(
                        "Spring Batch"
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_응답이_JSON이_아니면_예외가_발생한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(
                                "Spring Batch 내용을 정리했습니다."
                        )
                );

        // when & then
        assertThatThrownBy(() ->
                service.summarize(
                        "Spring Batch"
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_응답이_null이면_예외가_발생한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        when(aiClient.chat(any()))
                .thenReturn(
                        null
                );

        // when & then
        assertThatThrownBy(() ->
                service.summarize(
                        "Spring Batch"
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_응답의_choices가_비어있으면_예외가_발생한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        when(aiClient.chat(any()))
                .thenReturn(
                        new ChatCompletionResponse(
                                List.of()
                        )
                );

        // when & then
        assertThatThrownBy(() ->
                service.summarize(
                        "Spring Batch"
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_응답의_message가_null이면_예외가_발생한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        ChatCompletionResponse response =
                new ChatCompletionResponse(
                        List.of(
                                new Choice(
                                        null
                                )
                        )
                );

        when(aiClient.chat(any()))
                .thenReturn(
                        response
                );

        // when & then
        assertThatThrownBy(() ->
                service.summarize(
                        "Spring Batch"
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_응답_내용이_비어있으면_예외가_발생한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(
                                "   "
                        )
                );

        // when & then
        assertThatThrownBy(() ->
                service.summarize(
                        "Spring Batch"
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    private ChatCompletionResponse responseOf(
            String content
    ) {
        return new ChatCompletionResponse(
                List.of(
                        new Choice(
                                new Message(
                                        "assistant",
                                        content
                                )
                        )
                )
        );
    }
}