package io.github.kaltz.feldbuch.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kaltz.feldbuch.ai.client.AiClient;
import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeMergeResponse;
import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionRequest;
import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionResponse;
import io.github.kaltz.feldbuch.ai.dto.openai.Choice;
import io.github.kaltz.feldbuch.ai.dto.openai.Message;
import io.github.kaltz.feldbuch.common.exception.CustomException;
import io.github.kaltz.feldbuch.config.OpenAiProperties;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.knowledge.entity.Knowledge;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeCategory;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpenAiKnowledgeMergeServiceTest {

    private AiClient aiClient;

    private OpenAiProperties properties;

    private OpenAiKnowledgeMergeService service;

    private KnowledgeNote consolidatedNote;

    private KnowledgeNote incrementalNote;

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
                new OpenAiKnowledgeMergeService(
                        aiClient,
                        properties,
                        new ObjectMapper()
                );

        User user =
                User.builder()
                        .email("test@test.com")
                        .password("password")
                        .nickname("tester")
                        .role(UserRole.USER)
                        .build();

        ReflectionTestUtils.setField(
                user,
                "id",
                1L
        );

        Conversation conversation =
                Conversation.create(
                        user,
                        "Spring Batch 학습"
                );

        ReflectionTestUtils.setField(
                conversation,
                "id",
                10L
        );

        Knowledge root =
                Knowledge.createRoot(
                        user,
                        "WEB_DEVELOPMENT"
                );

        ReflectionTestUtils.setField(
                root,
                "id",
                100L
        );

        Knowledge springBatch =
                Knowledge.createChild(
                        user,
                        root,
                        "Spring Batch"
                );

        ReflectionTestUtils.setField(
                springBatch,
                "id",
                101L
        );

        consolidatedNote =
                KnowledgeNote.createConsolidated(
                        user,
                        conversation,
                        springBatch,
                        "Spring Batch 기본 구조",
                        "Job과 Step을 중심으로 정리한 통합 노트",
                        "Spring Batch는 Job과 Step을 중심으로 배치 작업을 구성합니다.",
                        List.of(
                                "Spring Batch",
                                "Job",
                                "Step"
                        )
                );

        ReflectionTestUtils.setField(
                consolidatedNote,
                "id",
                200L
        );

        incrementalNote =
                KnowledgeNote.createIncremental(
                        user,
                        conversation,
                        springBatch,
                        "Tasklet과 Chunk 처리 방식",
                        "두 가지 Step 처리 방식의 차이를 정리한 증분 노트",
                        "Tasklet은 단일 작업을 수행하고 Chunk는 여러 Item을 일정 단위로 처리합니다.",
                        List.of(
                                "Spring Batch",
                                "Tasklet",
                                "Chunk"
                        )
                );

        ReflectionTestUtils.setField(
                incrementalNote,
                "id",
                201L
        );
    }

    @Test
    void 통합_노트와_증분_노트를_AI로_병합한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": "Spring Batch 실행 구조와 처리 방식",
                  "description": "Job과 Step부터 Tasklet과 Chunk까지 핵심 실행 구조를 정리한 통합 노트",
                  "summary": "Spring Batch에서 Job은 전체 배치 작업을 나타내고 Step은 실제 처리 단위를 담당합니다. Step은 Tasklet 또는 Chunk 방식으로 구성할 수 있습니다.",
                  "keywords": [
                    "Spring Batch",
                    "Job",
                    "Step",
                    "Tasklet",
                    "Chunk"
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
        AiKnowledgeMergeResponse result =
                service.merge(
                        consolidatedNote,
                        incrementalNote
                );

        // then
        assertThat(result.category())
                .isEqualTo(
                        KnowledgeCategory.SPRING_BATCH
                );

        assertThat(result.title())
                .isEqualTo(
                        "Spring Batch 실행 구조와 처리 방식"
                );

        assertThat(result.description())
                .isEqualTo(
                        "Job과 Step부터 Tasklet과 Chunk까지 핵심 실행 구조를 정리한 통합 노트"
                );

        assertThat(result.summary())
                .contains(
                        "Job",
                        "Step",
                        "Tasklet",
                        "Chunk"
                );

        assertThat(result.keywords())
                .containsExactly(
                        "Spring Batch",
                        "Job",
                        "Step",
                        "Tasklet",
                        "Chunk"
                );
    }

    @Test
    void AI_병합_요청에_시스템_프롬프트와_두_노트의_내용을_포함한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": "Spring Batch 통합 노트",
                  "description": "Spring Batch 내용을 병합한 노트",
                  "summary": "기존 내용과 신규 내용을 병합했습니다.",
                  "keywords": [
                    "Spring Batch",
                    "Tasklet",
                    "Chunk"
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
        service.merge(
                consolidatedNote,
                incrementalNote
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
                        consolidatedNote.getTitle(),
                        consolidatedNote.getDescription(),
                        consolidatedNote.getSummary(),
                        incrementalNote.getTitle(),
                        incrementalNote.getDescription(),
                        incrementalNote.getSummary()
                );

        assertThat(userMessage.content())
                .contains(
                        "<consolidated-note>",
                        "</consolidated-note>",
                        "<incremental-note>",
                        "</incremental-note>"
                );
    }

    @Test
    void AI_병합_응답의_카테고리를_enum으로_변환한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": "Spring Batch",
                  "description": "Spring Batch 통합 설명",
                  "summary": "Spring Batch 통합 요약",
                  "keywords": [
                    "Spring Batch",
                    "Tasklet",
                    "Chunk"
                  ]
                }
                """;

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(json)
                );

        // when
        AiKnowledgeMergeResponse result =
                service.merge(
                        consolidatedNote,
                        incrementalNote
                );

        // then
        assertThat(result.category())
                .isSameAs(
                        KnowledgeCategory.SPRING_BATCH
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
                  "description": "병합 설명",
                  "summary": "병합 요약",
                  "keywords": [
                    "Spring Batch",
                    "Tasklet",
                    "Chunk"
                  ]
                }
                """;

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(json)
                );

        // when & then
        assertThatThrownBy(() ->
                service.merge(
                        consolidatedNote,
                        incrementalNote
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_병합_응답의_카테고리가_null이면_예외가_발생한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        String json = """
                {
                  "category": null,
                  "title": "Spring Batch",
                  "description": "병합 설명",
                  "summary": "병합 요약",
                  "keywords": [
                    "Spring Batch",
                    "Tasklet",
                    "Chunk"
                  ]
                }
                """;

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(json)
                );

        // when & then
        assertThatThrownBy(() ->
                service.merge(
                        consolidatedNote,
                        incrementalNote
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_병합_응답의_제목이_비어있으면_예외가_발생한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": " ",
                  "description": "병합 설명",
                  "summary": "병합 요약",
                  "keywords": [
                    "Spring Batch",
                    "Tasklet",
                    "Chunk"
                  ]
                }
                """;

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(json)
                );

        // when & then
        assertThatThrownBy(() ->
                service.merge(
                        consolidatedNote,
                        incrementalNote
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_병합_응답의_설명이_비어있으면_예외가_발생한다() {
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
                  "summary": "병합 요약",
                  "keywords": [
                    "Spring Batch",
                    "Tasklet",
                    "Chunk"
                  ]
                }
                """;

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(json)
                );

        // when & then
        assertThatThrownBy(() ->
                service.merge(
                        consolidatedNote,
                        incrementalNote
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_병합_응답의_요약이_비어있으면_예외가_발생한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": "Spring Batch",
                  "description": "병합 설명",
                  "summary": null,
                  "keywords": [
                    "Spring Batch",
                    "Tasklet",
                    "Chunk"
                  ]
                }
                """;

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(json)
                );

        // when & then
        assertThatThrownBy(() ->
                service.merge(
                        consolidatedNote,
                        incrementalNote
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_병합_응답의_키워드가_3개_미만이면_예외가_발생한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": "Spring Batch",
                  "description": "병합 설명",
                  "summary": "병합 요약",
                  "keywords": [
                    "Spring Batch",
                    "Chunk"
                  ]
                }
                """;

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(json)
                );

        // when & then
        assertThatThrownBy(() ->
                service.merge(
                        consolidatedNote,
                        incrementalNote
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_병합_응답의_키워드가_7개를_초과하면_예외가_발생한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": "Spring Batch",
                  "description": "병합 설명",
                  "summary": "병합 요약",
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
                service.merge(
                        consolidatedNote,
                        incrementalNote
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_병합_응답의_키워드에_빈_값이_있으면_예외가_발생한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": "Spring Batch",
                  "description": "병합 설명",
                  "summary": "병합 요약",
                  "keywords": [
                    "Spring Batch",
                    " ",
                    "Chunk"
                  ]
                }
                """;

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(json)
                );

        // when & then
        assertThatThrownBy(() ->
                service.merge(
                        consolidatedNote,
                        incrementalNote
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_병합_응답의_키워드가_중복되면_예외가_발생한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": "Spring Batch",
                  "description": "병합 설명",
                  "summary": "병합 요약",
                  "keywords": [
                    "Spring Batch",
                    "Chunk",
                    "Chunk"
                  ]
                }
                """;

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(json)
                );

        // when & then
        assertThatThrownBy(() ->
                service.merge(
                        consolidatedNote,
                        incrementalNote
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_병합_응답이_JSON이_아니면_예외가_발생한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(
                                "두 노트를 병합했습니다."
                        )
                );

        // when & then
        assertThatThrownBy(() ->
                service.merge(
                        consolidatedNote,
                        incrementalNote
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_병합_응답이_null이면_예외가_발생한다() {
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
                service.merge(
                        consolidatedNote,
                        incrementalNote
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_병합_응답의_choices가_비어있으면_예외가_발생한다() {
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
                service.merge(
                        consolidatedNote,
                        incrementalNote
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_병합_응답의_message가_null이면_예외가_발생한다() {
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
                service.merge(
                        consolidatedNote,
                        incrementalNote
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_병합_응답_내용이_비어있으면_예외가_발생한다() {
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
                service.merge(
                        consolidatedNote,
                        incrementalNote
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