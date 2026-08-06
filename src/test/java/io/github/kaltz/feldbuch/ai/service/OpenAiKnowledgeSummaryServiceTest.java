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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OpenAiKnowledgeSummaryServiceTest {

    private AiClient aiClient;

    private OpenAiProperties properties;

    private OpenAiKnowledgeSummaryService service;

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
                new OpenAiKnowledgeSummaryService(
                        aiClient,
                        properties,
                        new ObjectMapper()
                );

        when(
                properties.getModel()
        ).thenReturn(
                "gpt-4.1-mini"
        );
    }

    @Test
    @DisplayName("대화를 Markdown 학습 노트로 요약한다.")
    void summarizeConversation() {
        // given
        String conversation = """
                USER:
                Spring Batch에서 Job과 Step은 어떤 역할을 하나?
                
                ASSISTANT:
                Job은 전체 배치 실행 단위이고,
                Step은 실제 작업을 수행하는 단위입니다.
                """;

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
        AiKnowledgeSummaryResponse result =
                service.summarize(
                        conversation
                );

        // then
        assertThat(result.category())
                .isEqualTo(
                        KnowledgeCategory.SPRING_BATCH
                );

        assertThat(result.title())
                .isEqualTo(
                        "Spring Batch의 Job과 Step"
                );

        assertThat(result.description())
                .isEqualTo(
                        "Spring Batch의 실행 단위와 처리 흐름을 정리한 학습 노트"
                );

        assertThat(result.summary())
                .contains(
                        "## 개념",
                        "## 동작 원리",
                        "## 주요 특징",
                        "## 예시",
                        "## 정리"
                );

        assertThat(result.summary())
                .contains(
                        "```java",
                        "JobBuilder",
                        "StepBuilder",
                        "```"
                );

        assertThat(result.summary().length())
                .isGreaterThanOrEqualTo(
                        300
                );

        assertThat(result.keywords())
                .containsExactly(
                        "Spring Batch",
                        "Job",
                        "Step",
                        "JobBuilder",
                        "StepBuilder"
                );
    }

    @Test
    @DisplayName("AI 요청에 모델과 시스템 및 사용자 프롬프트를 포함한다.")
    void createRequest() {
        // given
        String conversation = """
                Spring Batch의 Job과 Step을 설명해줘.
                
                ```java
                return new JobBuilder(
                        "sampleJob",
                        jobRepository
                )
                        .start(sampleStep)
                        .build();
                ```
                """;

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
                        "\"title\"",
                        "\"description\"",
                        "\"summary\"",
                        "\"keywords\""
                );

        assertThat(systemMessage.content())
                .contains(
                        "Markdown",
                        "최소 500자",
                        "코드 블록",
                        "대화에 코드가 포함되어 있다면"
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
                        "<conversation>",
                        "</conversation>",
                        conversation.trim()
                );

        assertThat(userMessage.content())
                .contains(
                        "Markdown 코드 블록",
                        "대화에 없는 사실이나 코드를 새로 만들어내지 마세요"
                );
    }

    @Test
    @DisplayName("summary가 300자 미만이면 예외가 발생한다.")
    void summaryIsTooShort() {
        // given
        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": "Spring Batch",
                  "description": "Spring Batch 학습 노트",
                  "summary": "## 개념\\n\\nSpring Batch는 Job과 Step으로 구성됩니다.",
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
    @DisplayName("summary에 Markdown 소제목이 없으면 예외가 발생한다.")
    void summaryHasNoMarkdownHeading() {
        // given
        String summary =
                longPlainTextSummary();

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
    @DisplayName("Markdown 코드 블록이 닫히지 않으면 예외가 발생한다.")
    void codeFenceIsNotClosed() {
        // given
        String summary =
                validMarkdownSummary()
                        + """
                        
                        ## 잘못된 코드 블록
                        
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
                responseOf(json)
        );

        // when & then
        assertThatThrownBy(() ->
                service.summarize(
                        "Spring Batch 예제 코드를 설명해줘."
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    @DisplayName("코드 블록이 없는 Markdown 학습 문서는 정상적으로 처리한다.")
    void markdownWithoutCodeFence() {
        // given
        String json =
                responseJson(
                        "SPRING_BATCH",
                        validMarkdownSummaryWithoutCode(),
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
                responseOf(json)
        );

        // when
        AiKnowledgeSummaryResponse result =
                service.summarize(
                        "Spring Batch에서 Job과 Step을 설명해줘."
                );

        // then
        assertThat(result.summary())
                .contains(
                        "## 개념",
                        "## 동작 원리",
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
                  "title": "Spring Batch",
                  "description": "Spring Batch 학습 노트",
                  "summary": %s,
                  "keywords": [
                    "Spring Batch",
                    "Job",
                    "Step"
                  ]
                }
                """.formatted(
                toJsonString(
                        validMarkdownSummary()
                )
        );

        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
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
    @DisplayName("지원하지 않는 category가 반환되면 예외가 발생한다.")
    void unsupportedCategory() {
        // given
        String json =
                responseJson(
                        "SPRING_BATCH_FRAMEWORK",
                        validMarkdownSummary(),
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
    @DisplayName("제목이 비어 있으면 예외가 발생한다.")
    void titleIsBlank() {
        // given
        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": " ",
                  "description": "Spring Batch 학습 노트",
                  "summary": %s,
                  "keywords": [
                    "Spring Batch",
                    "Job",
                    "Step"
                  ]
                }
                """.formatted(
                toJsonString(
                        validMarkdownSummary()
                )
        );

        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
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
    @DisplayName("설명이 비어 있으면 예외가 발생한다.")
    void descriptionIsBlank() {
        // given
        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": "Spring Batch",
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
                        validMarkdownSummary()
                )
        );

        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
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
    @DisplayName("summary가 null이면 예외가 발생한다.")
    void summaryIsNull() {
        // given
        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": "Spring Batch",
                  "description": "Spring Batch 학습 노트",
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
    @DisplayName("키워드가 null이면 예외가 발생한다.")
    void keywordsAreNull() {
        // given
        String json = """
                {
                  "category": "SPRING_BATCH",
                  "title": "Spring Batch",
                  "description": "Spring Batch 학습 노트",
                  "summary": %s,
                  "keywords": null
                }
                """.formatted(
                toJsonString(
                        validMarkdownSummary()
                )
        );

        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
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
    @DisplayName("키워드가 3개 미만이면 예외가 발생한다.")
    void keywordCountIsLessThanThree() {
        // given
        String json =
                responseJson(
                        "SPRING_BATCH",
                        validMarkdownSummary(),
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
    @DisplayName("키워드가 7개를 초과하면 예외가 발생한다.")
    void keywordCountExceedsSeven() {
        // given
        String json =
                responseJson(
                        "SPRING_BATCH",
                        validMarkdownSummary(),
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
    @DisplayName("키워드에 빈 값이 포함되면 예외가 발생한다.")
    void keywordContainsBlank() {
        // given
        String json =
                responseJson(
                        "SPRING_BATCH",
                        validMarkdownSummary(),
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
    @DisplayName("대소문자만 다른 중복 키워드가 있으면 예외가 발생한다.")
    void keywordsContainDuplicateIgnoringCase() {
        // given
        String json =
                responseJson(
                        "SPRING_BATCH",
                        validMarkdownSummary(),
                        List.of(
                                "Spring Batch",
                                "Job",
                                "job"
                        )
                );

        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
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
    @DisplayName("AI 응답이 JSON이 아니면 예외가 발생한다.")
    void responseIsNotJson() {
        // given
        when(
                aiClient.chat(
                        any(ChatCompletionRequest.class)
                )
        ).thenReturn(
                responseOf(
                        "Spring Batch 학습 노트를 작성했습니다."
                )
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
                service.summarize(
                        "Spring Batch를 설명해줘."
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    @DisplayName("AI 응답의 choices가 비어 있으면 예외가 발생한다.")
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
                service.summarize(
                        "Spring Batch를 설명해줘."
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    @DisplayName("AI 응답의 message가 null이면 예외가 발생한다.")
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
                service.summarize(
                        "Spring Batch를 설명해줘."
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
                service.summarize(
                        "Spring Batch를 설명해줘."
                )
        )
                .isInstanceOf(
                        CustomException.class
                );
    }

    @Test
    @DisplayName("요약할 대화가 null이면 예외가 발생한다.")
    void conversationIsNull() {
        assertThatThrownBy(() ->
                service.summarize(
                        null
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "요약할 대화 내용은 필수입니다."
                );

        verify(
                aiClient,
                never()
        ).chat(
                any(ChatCompletionRequest.class)
        );
    }

    @Test
    @DisplayName("요약할 대화가 공백이면 예외가 발생한다.")
    void conversationIsBlank() {
        assertThatThrownBy(() ->
                service.summarize(
                        "   "
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "요약할 대화 내용은 필수입니다."
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
                validMarkdownSummary(),
                List.of(
                        "Spring Batch",
                        "Job",
                        "Step",
                        "JobBuilder",
                        "StepBuilder"
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
                            new AiKnowledgeSummaryResponse(
                                    KnowledgeCategory.valueOf(
                                            category
                                    ),
                                    "Spring Batch의 Job과 Step",
                                    "Spring Batch의 실행 단위와 처리 흐름을 정리한 학습 노트",
                                    summary,
                                    keywords
                            )
                    );
        } catch (IllegalArgumentException exception) {
            return """
                    {
                      "category": "%s",
                      "title": "Spring Batch의 Job과 Step",
                      "description": "Spring Batch의 실행 단위와 처리 흐름을 정리한 학습 노트",
                      "summary": %s,
                      "keywords": %s
                    }
                    """.formatted(
                    category,
                    toJsonString(summary),
                    toJsonArray(keywords)
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

    private String validMarkdownSummary() {
        return """
                ## 개념
                
                Spring Batch는 반복적이거나 대용량인 데이터를 안정적으로 처리하기 위한 배치 프레임워크입니다.
                하나의 배치 작업 전체는 **Job**으로 표현하며, Job 내부에는 실제 처리 단위인 하나 이상의
                **Step**이 포함됩니다. Job과 Step을 분리하면 전체 실행 흐름과 개별 처리 로직을 나누어
                구성할 수 있어 작업의 책임이 명확해집니다.
                
                ## 동작 원리
                
                Job이 실행되면 설정된 순서에 따라 Step이 실행됩니다. 각 Step은 Tasklet 방식이나
                Chunk 방식으로 구현할 수 있습니다. Tasklet은 하나의 작업을 직접 수행하는 데 적합하고,
                Chunk는 데이터를 일정 개수씩 읽고 처리한 뒤 저장하는 방식에 적합합니다.
                
                실행 흐름은 다음과 같습니다.
                
                1. JobLauncher가 Job을 실행합니다.
                2. Job은 등록된 Step을 순서대로 시작합니다.
                3. Step은 정의된 처리 로직을 수행합니다.
                4. Spring Batch는 Job과 Step의 실행 결과를 메타데이터 테이블에 기록합니다.
                5. 실패한 작업은 저장된 실행 상태를 이용해 재시작할 수 있습니다.
                
                ## 주요 특징
                
                - Job은 전체 배치 작업의 실행 단위입니다.
                - Step은 실제 비즈니스 처리가 수행되는 단위입니다.
                - JobParameters를 통해 실행 인스턴스를 구분할 수 있습니다.
                - 실행 상태를 저장하므로 실패 지점부터 재시작하는 구조를 만들 수 있습니다.
                - 처리 목적에 따라 Tasklet 방식과 Chunk 방식을 선택할 수 있습니다.
                
                ## 예시
                
                다음 코드는 하나의 Step으로 구성된 Job을 등록하는 기본적인 형태입니다.
                
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
                
                JobBuilder는 Job의 실행 구조를 정의하고, StepBuilder는 실제 처리 단위를 구성합니다.
                예제의 Tasklet은 한 번 실행된 후 `RepeatStatus.FINISHED`를 반환하여 Step을 종료합니다.
                
                ## 주의사항
                
                같은 Job 이름과 동일한 JobParameters 조합으로 이미 완료된 Job을 다시 실행하면
                같은 JobInstance로 판단될 수 있습니다. 반복 실행이 필요한 작업이라면 실행 시각이나
                별도의 식별값을 JobParameters에 포함하여 새로운 실행 단위를 만들 필요가 있습니다.
                
                ## 정리
                
                Spring Batch에서 Job은 전체 배치 흐름을 담당하고 Step은 실제 작업을 담당합니다.
                두 개념을 분리해 이해하면 배치 실행 구조, 실패 복구, 재실행, 처리 방식 선택을
                체계적으로 설계할 수 있습니다.
                """;
    }

    private String validMarkdownSummaryWithoutCode() {
        return """
                ## 개념
                
                Spring Batch는 반복적이고 대량으로 수행되는 작업을 안정적으로 처리하기 위한
                배치 프레임워크입니다. 배치 작업 전체는 Job으로 표현하며, 실제 작업은 Job 내부의
                Step에서 수행됩니다. Job과 Step을 분리하면 전체 실행 흐름과 개별 처리 로직의
                책임을 명확하게 나눌 수 있습니다.
                
                ## 동작 원리
                
                Job이 실행되면 등록된 Step이 순서대로 처리됩니다. 각 Step은 Tasklet 방식이나
                Chunk 방식으로 구성할 수 있습니다. Tasklet 방식은 하나의 독립적인 작업을 실행할 때
                유용하며, Chunk 방식은 여러 데이터를 일정 개수씩 나누어 읽고 처리하고 저장할 때
                적합합니다.
                
                Spring Batch는 Job과 Step의 시작 시각, 종료 시각, 상태와 실행 결과를 메타데이터
                테이블에 기록합니다. 이를 통해 작업의 성공 여부를 확인하고 실패한 작업을 재시작할
                수 있습니다.
                
                ## 주요 특징
                
                - Job은 전체 실행 흐름을 정의합니다.
                - Step은 실제 처리 로직을 담당합니다.
                - JobParameters는 실행 인스턴스를 구분합니다.
                - 실행 상태가 저장되므로 실패 복구가 가능합니다.
                - Tasklet과 Chunk 중 작업 목적에 맞는 방식을 선택할 수 있습니다.
                
                ## 사용하는 이유
                
                일반적인 요청과 응답 중심의 웹 처리만으로는 많은 데이터를 장시간 안정적으로
                처리하기 어렵습니다. Spring Batch는 트랜잭션, 실행 상태, 실패 복구와 재시작 기능을
                제공하여 대량 작업을 예측 가능한 구조로 관리하도록 돕습니다.
                
                ## 주의사항
                
                동일한 Job 이름과 동일한 JobParameters로 완료된 작업을 다시 실행하면 이미 완료된
                JobInstance로 판단될 수 있습니다. 반복 실행이 필요한 경우에는 실행마다 달라지는
                식별값을 JobParameters에 포함해야 합니다.
                
                ## 정리
                
                Job은 전체 배치 작업이며 Step은 실제 처리 단위입니다. 두 구조의 역할을 구분하면
                배치 처리 흐름과 실패 복구 방식을 더 쉽게 설계할 수 있습니다.
                """;
    }

    private String longPlainTextSummary() {
        return """
                Spring Batch는 반복적이거나 대용량인 데이터를 안정적으로 처리하기 위한 배치
                프레임워크입니다. 하나의 전체 작업은 Job으로 표현되고 실제 처리 로직은 Job 내부의
                Step에서 실행됩니다. Job과 Step을 구분하면 전체 실행 흐름과 개별 처리 책임을
                분리할 수 있습니다.
                
                Job이 실행되면 등록된 Step들이 정의된 순서대로 시작됩니다. Step은 하나의 작업을
                직접 수행하는 Tasklet 방식이나 데이터를 일정 단위로 나누어 처리하는 Chunk 방식으로
                구성할 수 있습니다. Chunk 방식에서는 ItemReader가 데이터를 읽고 ItemProcessor가
                데이터를 가공한 뒤 ItemWriter가 처리된 데이터를 저장합니다.
                
                Spring Batch는 Job과 Step의 실행 상태를 메타데이터 테이블에 저장합니다. 따라서
                실행 성공 여부와 실패 지점을 확인할 수 있고, 실패한 작업을 재시작하는 구조도
                만들 수 있습니다. JobParameters는 동일한 Job의 실행 인스턴스를 구분하는 데
                사용됩니다.
                
                동일한 Job 이름과 동일한 JobParameters로 이미 완료된 작업을 다시 실행하면 같은
                JobInstance로 판단될 수 있습니다. 반복 실행이 필요한 경우에는 실행마다 다른
                식별값을 전달해야 합니다. Job은 전체 흐름을 담당하고 Step은 실제 처리를 담당한다는
                역할 구분이 Spring Batch 구조를 이해하는 핵심입니다.
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