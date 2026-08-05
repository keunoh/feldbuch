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
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeRootCategory;
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

        Knowledge knowledge =
                Knowledge.createRoot(
                        user,
                        "웹 개발"
                );

        consolidatedNote =
                KnowledgeNote.createConsolidated(
                        user,
                        conversation,
                        knowledge,
                        "Spring Batch 종합 정리",
                        "Spring Batch의 전체 구조를 정리한 통합 노트",
                        "Spring Batch는 Job과 Step으로 작업을 구성합니다.",
                        List.of(
                                "Spring Batch",
                                "Job",
                                "Step"
                        )
                );

        incrementalNote =
                KnowledgeNote.createIncremental(
                        user,
                        conversation,
                        knowledge,
                        "Tasklet과 Chunk 방식",
                        "Tasklet과 Chunk의 차이를 정리한 증분 노트",
                        "Tasklet은 단일 작업을 처리하고 Chunk는 여러 Item을 묶어서 처리합니다.",
                        List.of(
                                "Tasklet",
                                "Chunk",
                                "Item"
                        )
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
                  "rootCategory": "WEB_DEVELOPMENT",
                  "knowledgePath": [
                    "Spring Framework",
                    "Spring Batch"
                  ],
                  "title": "Spring Batch 처리 구조",
                  "description": "Job, Step, Tasklet 및 Chunk 구조를 정리한 통합 노트",
                  "summary": "Spring Batch는 Job과 Step으로 구성되며 Step에서는 Tasklet 또는 Chunk 방식으로 작업을 처리할 수 있습니다.",
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

        // when
        AiKnowledgeMergeResponse result =
                service.merge(
                        consolidatedNote,
                        incrementalNote
                );

        // then
        assertThat(result.rootCategory())
                .isEqualTo(
                        KnowledgeRootCategory.WEB_DEVELOPMENT
                );

        assertThat(result.knowledgePath())
                .containsExactly(
                        "Spring Framework",
                        "Spring Batch"
                );

        assertThat(result.title())
                .isEqualTo(
                        "Spring Batch 처리 구조"
                );

        assertThat(result.description())
                .isEqualTo(
                        "Job, Step, Tasklet 및 Chunk 구조를 정리한 통합 노트"
                );

        assertThat(result.summary())
                .contains(
                        "Tasklet",
                        "Chunk"
                );

        assertThat(result.keywords())
                .containsExactly(
                        "Spring Batch",
                        "Tasklet",
                        "Chunk"
                );
    }

    @Test
    void AI_요청에_통합_노트와_증분_노트_내용을_포함한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        String json = """
                {
                  "rootCategory": "WEB_DEVELOPMENT",
                  "knowledgePath": ["Spring Framework", "Spring Batch"],
                  "title": "Spring Batch",
                  "description": "Spring Batch 통합 노트",
                  "summary": "병합된 요약",
                  "keywords": ["Spring Batch", "Tasklet", "Chunk"]
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

        assertThat(request.messages())
                .hasSize(2);

        String userPrompt =
                request.messages()
                        .get(1)
                        .content();

        assertThat(userPrompt)
                .contains(
                        consolidatedNote.getTitle(),
                        consolidatedNote.getDescription(),
                        consolidatedNote.getSummary(),
                        incrementalNote.getTitle(),
                        incrementalNote.getDescription(),
                        incrementalNote.getSummary()
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
                                "두 노트를 합쳤습니다."
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
    void AI_응답_내용이_비어있으면_예외가_발생한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf("   ")
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