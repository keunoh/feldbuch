package io.github.kaltz.feldbuch.knowledge.folder;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kaltz.feldbuch.ai.client.AiClient;
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
import static org.mockito.Mockito.*;

class OpenAiKnowledgeFolderSelectionServiceTest {

    private AiClient aiClient;

    private OpenAiProperties properties;

    private OpenAiKnowledgeFolderSelectionService service;

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
                new OpenAiKnowledgeFolderSelectionService(
                        aiClient,
                        properties,
                        new ObjectMapper()
                );
    }

    @Test
    void AI가_기존_폴더를_선택한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        List<KnowledgeFolderCandidate> candidates =
                List.of(
                        new KnowledgeFolderCandidate(
                                10L,
                                "Spring"
                        ),
                        new KnowledgeFolderCandidate(
                                11L,
                                "React"
                        )
                );

        String json = """
                {
                  "selectionType": "EXISTING",
                  "selectedKnowledgeId": 10
                }
                """;

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(json)
                );

        // when
        AiKnowledgeFolderSelectionResponse result =
                service.select(
                        "웹 개발",
                        "웹 개발",
                        "Spring Framework",
                        candidates
                );

        // then
        assertThat(result.selectionType())
                .isEqualTo(
                        AiKnowledgeFolderSelectionType.EXISTING
                );

        assertThat(result.selectedKnowledgeId())
                .isEqualTo(10L);
    }

    @Test
    void AI가_새_폴더_생성을_선택한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        List<KnowledgeFolderCandidate> candidates =
                List.of(
                        new KnowledgeFolderCandidate(
                                10L,
                                "React"
                        )
                );

        String json = """
                {
                  "selectionType": "CREATE",
                  "selectedKnowledgeId": null
                }
                """;

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(json)
                );

        // when
        AiKnowledgeFolderSelectionResponse result =
                service.select(
                        "웹 개발",
                        "웹 개발",
                        "Spring Framework",
                        candidates
                );

        // then
        assertThat(result.selectionType())
                .isEqualTo(
                        AiKnowledgeFolderSelectionType.CREATE
                );

        assertThat(result.selectedKnowledgeId())
                .isNull();
    }

    @Test
    void 후보가_없으면_AI를_호출하지_않고_CREATE를_반환한다() {
        // when
        AiKnowledgeFolderSelectionResponse result =
                service.select(
                        "웹 개발",
                        "웹 개발",
                        "Spring",
                        List.of()
                );

        // then
        assertThat(result.selectionType())
                .isEqualTo(
                        AiKnowledgeFolderSelectionType.CREATE
                );

        assertThat(result.selectedKnowledgeId())
                .isNull();

        verify(aiClient, never())
                .chat(any());
    }

    @Test
    void AI가_후보에_없는_ID를_선택하면_실패한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        List<KnowledgeFolderCandidate> candidates =
                List.of(
                        new KnowledgeFolderCandidate(
                                10L,
                                "Spring"
                        )
                );

        String json = """
                {
                  "selectionType": "EXISTING",
                  "selectedKnowledgeId": 999
                }
                """;

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(json)
                );

        // when & then
        assertThatThrownBy(() ->
                service.select(
                        "웹 개발",
                        "웹 개발",
                        "Spring Framework",
                        candidates
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void EXISTING인데_선택_ID가_null이면_실패한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        List<KnowledgeFolderCandidate> candidates =
                List.of(
                        new KnowledgeFolderCandidate(
                                10L,
                                "Spring"
                        )
                );

        String json = """
                {
                  "selectionType": "EXISTING",
                  "selectedKnowledgeId": null
                }
                """;

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(json)
                );

        // when & then
        assertThatThrownBy(() ->
                service.select(
                        "웹 개발",
                        "웹 개발",
                        "Spring Framework",
                        candidates
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void CREATE인데_선택_ID가_있으면_실패한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        List<KnowledgeFolderCandidate> candidates =
                List.of(
                        new KnowledgeFolderCandidate(
                                10L,
                                "Spring"
                        )
                );

        String json = """
                {
                  "selectionType": "CREATE",
                  "selectedKnowledgeId": 10
                }
                """;

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(json)
                );

        // when & then
        assertThatThrownBy(() ->
                service.select(
                        "웹 개발",
                        "웹 개발",
                        "Spring Framework",
                        candidates
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_응답이_JSON이_아니면_실패한다() {
        // given
        when(properties.getModel())
                .thenReturn(
                        "gpt-4.1-mini"
                );

        List<KnowledgeFolderCandidate> candidates =
                List.of(
                        new KnowledgeFolderCandidate(
                                10L,
                                "Spring"
                        )
                );

        when(aiClient.chat(any()))
                .thenReturn(
                        responseOf(
                                "Spring 폴더를 선택하세요."
                        )
                );

        // when & then
        assertThatThrownBy(() ->
                service.select(
                        "웹 개발",
                        "웹 개발",
                        "Spring Framework",
                        candidates
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    void AI_응답의_choices가_비어있으면_실패한다() {
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
                service.select(
                        "웹 개발",
                        "웹 개발",
                        "Spring Framework",
                        List.of(
                                new KnowledgeFolderCandidate(
                                        10L,
                                        "Spring"
                                )
                        )
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