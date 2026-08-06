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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OpenAiKnowledgeMergeServiceTest {

    private AiClient aiClient;

    private OpenAiProperties properties;

    private OpenAiKnowledgeMergeService service;

    private User user;

    private Conversation conversation;

    private Knowledge springBatchKnowledge;

    private KnowledgeNote consolidatedNote;

    private KnowledgeNote incrementalNote;

    @BeforeEach
    void setUp() {
        aiClient =
                mock(
                        AiClient.class
                );

        properties =
                mock(
                        OpenAiProperties.class
                );

        service =
                new OpenAiKnowledgeMergeService(
                        aiClient,
                        properties,
                        new ObjectMapper()
                );

        when(
                properties.getModel()
        ).thenReturn(
                "gpt-4.1-mini"
        );

        user =
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

        conversation =
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

        springBatchKnowledge =
                Knowledge.createChild(
                        user,
                        root,
                        "SPRING_BATCH"
                );

        ReflectionTestUtils.setField(
                springBatchKnowledge,
                "id",
                101L
        );

        consolidatedNote =
                KnowledgeNote.createConsolidated(
                        user,
                        conversation,
                        springBatchKnowledge,
                        "Spring Batch의 기본 구조",
                        "Job과 Step을 중심으로 Spring Batch의 실행 구조를 정리한 노트",
                        existingMarkdownSummary(),
                        List.of(
                                "Spring Batch",
                                "Job",
                                "Step"
                        )
                );

        ReflectionTestUtils.setField(
                consolidatedNote,
                "id",
                1000L
        );

        incrementalNote =
                KnowledgeNote.createIncremental(
                        user,
                        conversation,
                        springBatchKnowledge,
                        "Tasklet과 Chunk 처리 방식",
                        "Spring Batch Step의 두 가지 처리 방식을 비교한 노트",
                        incrementalMarkdownSummary(),
                        List.of(
                                "Spring Batch",
                                "Tasklet",
                                "Chunk"
                        )
                );

        ReflectionTestUtils.setField(
                incrementalNote,
                "id",
                1001L
        );
    }

    @Test
    @DisplayName("기존 통합 노트와 증분 노트를 Markdown 학습 문서로 병합한다.")
    void mergeKnowledgeNotes() {
        // given
        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
                responseOf(
                        validResponseJson()
                )
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
                        "Spring Batch의 실행 구조와 처리 방식"
                );

        assertThat(result.description())
                .isEqualTo(
                        "Job과 Step 구조부터 Tasklet과 Chunk 처리 방식까지 정리한 통합 학습 노트"
                );

        assertThat(result.summary())
                .contains(
                        "## 개념",
                        "## 동작 원리",
                        "## Tasklet 방식",
                        "## Chunk 방식",
                        "## 예시",
                        "## 주의사항",
                        "## 정리"
                );

        assertThat(result.summary())
                .contains(
                        "```java",
                        "JobBuilder",
                        "StepBuilder",
                        "chunk(",
                        "```"
                );

        assertThat(result.summary().length())
                .isGreaterThanOrEqualTo(
                        500
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
    @DisplayName("AI 요청에 병합 시스템 프롬프트와 두 노트의 내용을 포함한다.")
    void createMergeRequest() {
        // given
        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
                responseOf(
                        validResponseJson()
                )
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
                        "\"title\"",
                        "\"description\"",
                        "\"summary\"",
                        "\"keywords\""
                );

        assertThat(systemMessage.content())
                .contains(
                        "Markdown",
                        "최소 700자",
                        "기존 문서의 유효한",
                        "코드 블록",
                        "기존 통합 노트의 category 유지를 우선"
                );

        assertThat(systemMessage.content())
                .contains(
                        KnowledgeCategory.SPRING_BATCH.name()
                );

        assertThat(userMessage.role())
                .isEqualTo(
                        "user"
                );

        assertThat(userMessage.content())
                .contains(
                        "<consolidated-note>",
                        "</consolidated-note>",
                        "<incremental-note>",
                        "</incremental-note>"
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
                        "기존 노트의 내용을 새로운 노트만으로 덮어쓰거나",
                        "지나치게 짧게 축약하지 마세요"
                );
    }

    @Test
    @DisplayName("병합 summary가 500자 미만이면 예외가 발생한다.")
    void summaryIsTooShort() {
        // given
        String json =
                responseJson(
                        "SPRING_BATCH",
                        """
                                ## 개념
                                
                                Spring Batch는 Job과 Step으로 구성됩니다.
                                
                                ## 정리
                                
                                Tasklet과 Chunk는 Step의 처리 방식입니다.
                                """,
                        List.of(
                                "Spring Batch",
                                "Job",
                                "Step"
                        )
                );

        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
                responseOf(
                        json
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
    @DisplayName("병합 summary에 Markdown 소제목이 없으면 예외가 발생한다.")
    void summaryHasNoMarkdownHeading() {
        // given
        String json =
                responseJson(
                        "SPRING_BATCH",
                        longPlainTextSummary(),
                        List.of(
                                "Spring Batch",
                                "Job",
                                "Step"
                        )
                );

        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
                responseOf(
                        json
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
    @DisplayName("병합 summary의 코드 블록이 닫히지 않으면 예외가 발생한다.")
    void codeFenceIsNotClosed() {
        // given
        String summary =
                validMergedMarkdownSummary()
                        + """
                        
                        ## 잘못된 코드
                        
                        ```java
                        public class UnclosedExample {
                        }
                        """;

        String json =
                responseJson(
                        "SPRING_BATCH",
                        summary,
                        List.of(
                                "Spring Batch",
                                "Job",
                                "Step"
                        )
                );

        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
                responseOf(
                        json
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
    @DisplayName("코드가 없는 Markdown 통합 문서는 정상적으로 처리한다.")
    void markdownWithoutCodeFence() {
        // given
        String json =
                responseJson(
                        "SPRING_BATCH",
                        validMergedMarkdownSummaryWithoutCode(),
                        List.of(
                                "Spring Batch",
                                "Job",
                                "Step",
                                "Tasklet",
                                "Chunk"
                        )
                );

        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
                responseOf(
                        json
                )
        );

        // when
        AiKnowledgeMergeResponse result =
                service.merge(
                        consolidatedNote,
                        incrementalNote
                );

        // then
        assertThat(result.summary())
                .contains(
                        "## 개념",
                        "## Tasklet 방식",
                        "## Chunk 방식",
                        "## 정리"
                );

        assertThat(result.summary())
                .doesNotContain(
                        "```"
                );
    }

    @Test
    @DisplayName("category가 null이면 예외가 발생한다.")
    void categoryIsNull() {
        // given
        String json = """
                {
                  "category": null,
                  "title": "Spring Batch 통합 노트",
                  "description": "Spring Batch 학습 내용을 병합한 노트",
                  "summary": %s,
                  "keywords": [
                    "Spring Batch",
                    "Job",
                    "Step"
                  ]
                }
                """.formatted(
                toJsonString(
                        validMergedMarkdownSummary()
                )
        );

        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
                responseOf(
                        json
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
    @DisplayName("지원하지 않는 category가 반환되면 예외가 발생한다.")
    void unsupportedCategory() {
        // given
        String json =
                responseJson(
                        "SPRING_BATCH_FRAMEWORK",
                        validMergedMarkdownSummary(),
                        List.of(
                                "Spring Batch",
                                "Job",
                                "Step"
                        )
                );

        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
                responseOf(
                        json
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
    @DisplayName("제목이 비어 있으면 예외가 발생한다.")
    void titleIsBlank() {
        // given
        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": " ",
                  "description": "Spring Batch 학습 내용을 병합한 노트",
                  "summary": %s,
                  "keywords": [
                    "Spring Batch",
                    "Job",
                    "Step"
                  ]
                }
                """.formatted(
                toJsonString(
                        validMergedMarkdownSummary()
                )
        );

        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
                responseOf(
                        json
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
    @DisplayName("설명이 비어 있으면 예외가 발생한다.")
    void descriptionIsBlank() {
        // given
        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": "Spring Batch 통합 노트",
                  "description": "",
                  "summary": %s,
                  "keywords": [
                    "Spring Batch",
                    "Job",
                    "Step"
                  ]
                }
                """.formatted(
                toJsonString(
                        validMergedMarkdownSummary()
                )
        );

        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
                responseOf(
                        json
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
    @DisplayName("summary가 null이면 예외가 발생한다.")
    void summaryIsNull() {
        // given
        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": "Spring Batch 통합 노트",
                  "description": "Spring Batch 학습 내용을 병합한 노트",
                  "summary": null,
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
                responseOf(
                        json
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
    @DisplayName("키워드가 null이면 예외가 발생한다.")
    void keywordsAreNull() {
        // given
        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": "Spring Batch 통합 노트",
                  "description": "Spring Batch 학습 내용을 병합한 노트",
                  "summary": %s,
                  "keywords": null
                }
                """.formatted(
                toJsonString(
                        validMergedMarkdownSummary()
                )
        );

        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
                responseOf(
                        json
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
    @DisplayName("키워드가 3개 미만이면 예외가 발생한다.")
    void keywordCountIsLessThanThree() {
        // given
        String json =
                responseJson(
                        "SPRING_BATCH",
                        validMergedMarkdownSummary(),
                        List.of(
                                "Spring Batch",
                                "Job"
                        )
                );

        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
                responseOf(
                        json
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
    @DisplayName("키워드가 7개를 초과하면 예외가 발생한다.")
    void keywordCountExceedsSeven() {
        // given
        String json =
                responseJson(
                        "SPRING_BATCH",
                        validMergedMarkdownSummary(),
                        List.of(
                                "Spring Batch",
                                "Job",
                                "Step",
                                "Tasklet",
                                "Chunk",
                                "Reader",
                                "Processor",
                                "Writer"
                        )
                );

        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
                responseOf(
                        json
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
    @DisplayName("키워드에 빈 값이 있으면 예외가 발생한다.")
    void keywordContainsBlank() {
        // given
        String json =
                responseJson(
                        "SPRING_BATCH",
                        validMergedMarkdownSummary(),
                        List.of(
                                "Spring Batch",
                                " ",
                                "Step"
                        )
                );

        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
                responseOf(
                        json
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
    @DisplayName("대소문자만 다른 중복 키워드가 있으면 예외가 발생한다.")
    void keywordsContainDuplicateIgnoringCase() {
        // given
        String json =
                responseJson(
                        "SPRING_BATCH",
                        validMergedMarkdownSummary(),
                        List.of(
                                "Spring Batch",
                                "Chunk",
                                "chunk"
                        )
                );

        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
                responseOf(
                        json
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
    @DisplayName("AI 응답이 JSON이 아니면 예외가 발생한다.")
    void responseIsNotJson() {
        // given
        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
                responseOf(
                        "Spring Batch 노트를 병합했습니다."
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
    @DisplayName("AI 응답이 null이면 예외가 발생한다.")
    void responseIsNull() {
        // given
        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
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
    @DisplayName("AI 응답 choices가 비어 있으면 예외가 발생한다.")
    void choicesAreEmpty() {
        // given
        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
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
    @DisplayName("AI 응답 message가 null이면 예외가 발생한다.")
    void responseMessageIsNull() {
        // given
        ChatCompletionResponse response =
                new ChatCompletionResponse(
                        List.of(
                                new Choice(
                                        null
                                )
                        )
                );

        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
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
    @DisplayName("AI 응답 내용이 공백이면 예외가 발생한다.")
    void responseContentIsBlank() {
        // given
        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
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

    @Test
    @DisplayName("통합 노트가 null이면 예외가 발생한다.")
    void consolidatedNoteIsNull() {
        assertThatThrownBy(() ->
                service.merge(
                        null,
                        incrementalNote
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "통합 KnowledgeNote는 필수입니다."
                );

        verify(
                aiClient,
                never()
        ).chat(
                any(ChatCompletionRequest.class)
        );
    }

    @Test
    @DisplayName("첫 번째 노트가 증분 노트이면 예외가 발생한다.")
    void firstNoteMustBeConsolidated() {
        assertThatThrownBy(() ->
                service.merge(
                        incrementalNote,
                        incrementalNote
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "통합 노트 유형만 기존 노트로 사용할 수 있습니다."
                );

        verify(
                aiClient,
                never()
        ).chat(
                any(ChatCompletionRequest.class)
        );
    }

    @Test
    @DisplayName("증분 노트가 null이면 예외가 발생한다.")
    void incrementalNoteIsNull() {
        assertThatThrownBy(() ->
                service.merge(
                        consolidatedNote,
                        null
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "증분 KnowledgeNote는 필수입니다."
                );

        verify(
                aiClient,
                never()
        ).chat(
                any(ChatCompletionRequest.class)
        );
    }

    @Test
    @DisplayName("두 번째 노트가 통합 노트이면 예외가 발생한다.")
    void secondNoteMustBeIncremental() {
        assertThatThrownBy(() ->
                service.merge(
                        consolidatedNote,
                        consolidatedNote
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "증분 노트 유형만 신규 노트로 사용할 수 있습니다."
                );

        verify(
                aiClient,
                never()
        ).chat(
                any(ChatCompletionRequest.class)
        );
    }

    private String validResponseJson() {
        return responseJson(
                "SPRING_BATCH",
                validMergedMarkdownSummary(),
                List.of(
                        "Spring Batch",
                        "Job",
                        "Step",
                        "Tasklet",
                        "Chunk"
                )
        );
    }

    private String responseJson(
            String category,
            String summary,
            List<String> keywords
    ) {
        try {
            return new ObjectMapper()
                    .writeValueAsString(
                            new AiKnowledgeMergeResponse(
                                    KnowledgeCategory.valueOf(
                                            category
                                    ),
                                    "Spring Batch의 실행 구조와 처리 방식",
                                    "Job과 Step 구조부터 Tasklet과 Chunk 처리 방식까지 정리한 통합 학습 노트",
                                    summary,
                                    keywords
                            )
                    );
        } catch (IllegalArgumentException exception) {
            return """
                    {
                      "category": "%s",
                      "title": "Spring Batch의 실행 구조와 처리 방식",
                      "description": "Job과 Step 구조부터 Tasklet과 Chunk 처리 방식까지 정리한 통합 학습 노트",
                      "summary": %s,
                      "keywords": %s
                    }
                    """.formatted(
                    category,
                    toJsonString(
                            summary
                    ),
                    toJsonArray(
                            keywords
                    )
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "테스트 JSON 생성에 실패했습니다.",
                    exception
            );
        }
    }

    private String toJsonString(
            String value
    ) {
        try {
            return new ObjectMapper()
                    .writeValueAsString(
                            value
                    );
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "테스트 문자열 JSON 변환에 실패했습니다.",
                    exception
            );
        }
    }

    private String toJsonArray(
            List<String> values
    ) {
        try {
            return new ObjectMapper()
                    .writeValueAsString(
                            values
                    );
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "테스트 배열 JSON 변환에 실패했습니다.",
                    exception
            );
        }
    }

    private String existingMarkdownSummary() {
        return """
                ## 개념
                
                Spring Batch는 반복적이고 대량으로 처리되는 작업을 안정적으로 실행하기 위한
                배치 프레임워크입니다. 전체 배치 작업은 Job으로 표현되고, 실제 처리 로직은
                하나 이상의 Step으로 나누어 구성됩니다.
                
                ## 동작 원리
                
                JobLauncher가 Job을 실행하면 Job은 등록된 Step을 순서대로 실행합니다.
                각 Step의 성공 또는 실패 상태는 Spring Batch 메타데이터 테이블에 저장됩니다.
                
                ## 주요 특징
                
                - Job은 전체 실행 단위입니다.
                - Step은 실제 처리 단위입니다.
                - JobParameters를 통해 실행 인스턴스를 구분합니다.
                - 실행 이력을 바탕으로 실패 작업을 재시작할 수 있습니다.
                
                ## 정리
                
                Job과 Step의 역할을 분리하면 전체 실행 흐름과 개별 처리 책임을 명확하게
                구성할 수 있습니다.
                """;
    }

    private String incrementalMarkdownSummary() {
        return """
                ## Tasklet 방식
                
                Tasklet은 하나의 작업을 직접 수행하는 처리 방식입니다.
                파일 삭제, 임시 데이터 정리, 단순 상태 변경처럼 하나의 명확한 작업을 수행할 때
                적합합니다.
                
                ## Chunk 방식
                
                Chunk 방식은 일정 개수의 데이터를 읽고 처리한 뒤 한 번에 저장합니다.
                ItemReader, ItemProcessor, ItemWriter가 각각 읽기, 변환, 쓰기를 담당합니다.
                
                ## 차이점
                
                - Tasklet은 하나의 작업 단위를 직접 구현합니다.
                - Chunk는 대량 데이터를 일정 단위로 나누어 처리합니다.
                - 데이터 건수가 많은 경우 Chunk 방식이 트랜잭션 관리에 유리합니다.
                
                ## 정리
                
                단순 작업에는 Tasklet을 사용하고, 반복적인 대량 데이터 처리에는 Chunk 방식을
                사용하는 것이 적절합니다.
                """;
    }

    private String validMergedMarkdownSummary() {
        return """
                ## 개념
                
                Spring Batch는 반복적이거나 대량으로 수행되는 작업을 안정적으로 처리하기 위한
                배치 프레임워크입니다. 하나의 전체 배치 작업은 **Job**으로 표현되며,
                Job은 실제 처리 단위인 하나 이상의 **Step**으로 구성됩니다.
                
                Job과 Step을 분리하면 전체 실행 흐름과 개별 처리 로직의 책임이 명확해집니다.
                Spring Batch는 각 실행의 상태와 결과를 메타데이터 테이블에 저장하여 작업 이력,
                실패 복구와 재실행을 지원합니다.
                
                ## 동작 원리
                
                기본 실행 흐름은 다음과 같습니다.
                
                1. JobLauncher가 Job과 JobParameters를 전달받아 실행을 시작합니다.
                2. JobRepository가 JobInstance와 JobExecution 정보를 관리합니다.
                3. Job은 등록된 Step을 정의된 순서대로 실행합니다.
                4. 각 Step은 Tasklet 또는 Chunk 방식으로 실제 처리를 수행합니다.
                5. 실행 결과는 Spring Batch 메타데이터 테이블에 기록됩니다.
                6. 실패한 실행은 저장된 상태를 기준으로 재시작할 수 있습니다.
                
                ## Tasklet 방식
                
                Tasklet은 하나의 작업을 직접 수행하는 Step 구성 방식입니다.
                파일 삭제, 임시 데이터 정리, 특정 상태 갱신처럼 하나의 명확한 작업을
                실행할 때 적합합니다.
                
                Tasklet은 작업 수행 후 `RepeatStatus`를 반환합니다.
                `RepeatStatus.FINISHED`를 반환하면 해당 Step은 종료됩니다.
                
                ## Chunk 방식
                
                Chunk 방식은 여러 데이터를 일정 개수씩 나누어 처리하는 방식입니다.
                하나의 Chunk 안에서는 다음 순서가 반복됩니다.
                
                1. ItemReader가 데이터를 읽습니다.
                2. ItemProcessor가 데이터를 변환하거나 검증합니다.
                3. ItemWriter가 처리된 데이터를 저장합니다.
                4. 설정된 Chunk 크기만큼 처리가 완료되면 트랜잭션이 커밋됩니다.
                
                Chunk 크기를 적절하게 설정하면 대량 데이터를 한 번에 메모리에 올리지 않고
                안정적으로 처리할 수 있습니다.
                
                ## 주요 특징
                
                | 구분 | Tasklet | Chunk |
                |---|---|---|
                | 처리 단위 | 하나의 작업 | 여러 Item |
                | 주요 용도 | 파일 정리, 상태 변경 | 대량 데이터 처리 |
                | 트랜잭션 | 작업 단위 | Chunk 단위 |
                | 주요 구성 | Tasklet | Reader, Processor, Writer |
                
                ## 예시
                
                다음 코드는 Job과 Tasklet Step을 연결하는 기본 구조입니다.
                
                ```java
                @Bean
                public Job sampleJob(
                        JobRepository jobRepository,
                        Step sampleStep
                ) {
                    return new JobBuilder(
                            "sampleJob",
                            jobRepository
                    )
                            .start(sampleStep)
                            .build();
                }
                
                @Bean
                public Step sampleStep(
                        JobRepository jobRepository,
                        PlatformTransactionManager transactionManager
                ) {
                    return new StepBuilder(
                            "sampleStep",
                            jobRepository
                    )
                            .tasklet(
                                    (contribution, chunkContext) ->
                                            RepeatStatus.FINISHED,
                                    transactionManager
                            )
                            .build();
                }
                ```
                
                Chunk 방식의 Step은 다음과 같이 구성할 수 있습니다.
                
                ```java
                @Bean
                public Step chunkStep(
                        JobRepository jobRepository,
                        PlatformTransactionManager transactionManager,
                        ItemReader<String> reader,
                        ItemProcessor<String, String> processor,
                        ItemWriter<String> writer
                ) {
                    return new StepBuilder(
                            "chunkStep",
                            jobRepository
                    )
                            .<String, String>chunk(
                                    100,
                                    transactionManager
                            )
                            .reader(reader)
                            .processor(processor)
                            .writer(writer)
                            .build();
                }
                ```
                
                ## 주의사항
                
                같은 Job 이름과 동일한 JobParameters로 이미 완료된 작업을 다시 실행하면
                동일한 JobInstance로 판단될 수 있습니다. 반복 실행이 필요하다면 실행마다
                달라지는 값을 JobParameters에 포함해야 합니다.
                
                Chunk 크기가 지나치게 크면 한 트랜잭션에서 처리하는 데이터가 많아져
                메모리 사용량과 재처리 범위가 커질 수 있습니다. 반대로 너무 작으면
                커밋 횟수가 증가해 성능이 저하될 수 있습니다.
                
                ## 정리
                
                Job은 전체 배치 실행 흐름을 담당하고 Step은 실제 작업을 수행합니다.
                단순하고 독립적인 작업은 Tasklet 방식이 적합하며, 반복적인 대량 데이터 처리는
                Chunk 방식이 적합합니다. 작업 성격에 맞는 Step 구성을 선택하고,
                JobParameters와 실행 상태를 함께 관리해야 안정적인 배치 시스템을 만들 수 있습니다.
                """;
    }

    private String validMergedMarkdownSummaryWithoutCode() {
        return """
                ## 개념
                
                Spring Batch는 반복적이고 대량으로 수행되는 작업을 안정적으로 처리하기 위한
                배치 프레임워크입니다. 전체 작업은 Job으로 표현되며 실제 처리 로직은 하나 이상의
                Step으로 나누어 구성됩니다. Job과 Step을 구분하면 실행 흐름과 처리 책임을
                명확하게 분리할 수 있습니다.
                
                ## 동작 원리
                
                JobLauncher는 Job과 JobParameters를 사용하여 실행을 시작합니다.
                Job은 등록된 Step을 순서대로 실행하며 Spring Batch는 각 실행의 상태와 결과를
                메타데이터 테이블에 저장합니다. 저장된 실행 정보는 실패 분석과 작업 재시작에
                활용됩니다.
                
                ## Tasklet 방식
                
                Tasklet은 하나의 작업을 직접 구현하는 방식입니다. 파일 삭제, 임시 데이터 정리,
                단순 상태 변경처럼 처리 범위가 명확한 작업에 적합합니다. 작업이 끝나면
                완료 상태를 반환하여 Step 실행을 종료합니다.
                
                ## Chunk 방식
                
                Chunk 방식은 여러 데이터를 일정 개수씩 나누어 처리합니다. 데이터를 읽고,
                변환하고, 저장하는 역할을 각각 분리할 수 있으며 설정된 개수만큼 처리가 끝나면
                트랜잭션을 커밋합니다.
                
                ## 주요 특징
                
                - Job은 전체 배치 실행 단위입니다.
                - Step은 실제 처리 단위입니다.
                - Tasklet은 하나의 명확한 작업에 적합합니다.
                - Chunk는 반복적인 대량 데이터 처리에 적합합니다.
                - 실행 상태가 저장되므로 실패 작업을 재시작할 수 있습니다.
                - JobParameters로 각 실행 인스턴스를 구분할 수 있습니다.
                
                ## 주의사항
                
                동일한 Job 이름과 동일한 JobParameters로 완료된 작업을 다시 실행하면 같은
                JobInstance로 판단될 수 있습니다. 반복 실행이 필요하다면 실행마다 다른
                식별값을 JobParameters에 포함해야 합니다.
                
                Chunk 크기가 너무 크면 메모리 사용량과 실패 시 재처리 범위가 커질 수 있고,
                너무 작으면 트랜잭션 커밋 횟수가 늘어 성능이 저하될 수 있습니다.
                
                ## 정리
                
                Job은 전체 흐름을 담당하고 Step은 실제 처리를 담당합니다. 단순한 독립 작업에는
                Tasklet을 사용하고 반복적인 대량 데이터 처리에는 Chunk를 사용하는 것이 적합합니다.
                실행 이력과 JobParameters까지 함께 설계해야 안정적으로 재실행할 수 있습니다.
                """;
    }

    private String longPlainTextSummary() {
        return """
                Spring Batch는 반복적이고 대량으로 수행되는 작업을 안정적으로 처리하기 위한
                배치 프레임워크입니다. 전체 배치 작업은 Job으로 표현되며 실제 처리 로직은
                하나 이상의 Step으로 나누어 구성됩니다. Job과 Step을 구분하면 전체 실행 흐름과
                개별 처리 로직의 책임을 명확하게 나눌 수 있습니다.
                
                JobLauncher가 Job과 JobParameters를 전달받아 실행을 시작하면 JobRepository는
                JobInstance와 JobExecution 정보를 관리합니다. Job은 등록된 Step을 정의된 순서대로
                실행하고 각 Step은 Tasklet 방식이나 Chunk 방식으로 처리할 수 있습니다.
                
                Tasklet 방식은 파일 삭제나 상태 변경처럼 하나의 명확한 작업을 수행할 때 적합합니다.
                Chunk 방식은 여러 데이터를 일정 개수씩 읽고 처리한 뒤 저장할 때 적합합니다.
                Chunk 방식에서는 ItemReader가 데이터를 읽고 ItemProcessor가 데이터를 변환하며
                ItemWriter가 결과를 저장합니다.
                
                Spring Batch는 Job과 Step의 상태, 시작 시각, 종료 시각과 실행 결과를 메타데이터
                테이블에 저장합니다. 이 정보는 실패한 작업을 분석하고 재시작하는 데 사용됩니다.
                동일한 Job 이름과 동일한 JobParameters로 완료된 작업을 다시 실행하면 같은
                JobInstance로 판단될 수 있으므로 반복 실행이 필요하다면 실행마다 다른 식별값을
                전달해야 합니다.
                
                단순하고 독립적인 작업에는 Tasklet이 적합하고 반복적인 대량 데이터 처리에는
                Chunk가 적합합니다. Chunk 크기는 메모리 사용량, 트랜잭션 범위와 커밋 비용을
                고려하여 정해야 합니다. Job은 전체 흐름을 담당하고 Step은 실제 처리 로직을
                담당한다는 역할 구분이 Spring Batch를 이해하는 핵심입니다.
                """;
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