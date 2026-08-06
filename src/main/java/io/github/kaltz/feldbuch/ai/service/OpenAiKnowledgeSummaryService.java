package io.github.kaltz.feldbuch.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kaltz.feldbuch.ai.client.AiClient;
import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeSummaryResponse;
import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionRequest;
import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionResponse;
import io.github.kaltz.feldbuch.ai.dto.openai.Message;
import io.github.kaltz.feldbuch.ai.prompt.KnowledgeSummaryPrompt;
import io.github.kaltz.feldbuch.common.exception.CustomException;
import io.github.kaltz.feldbuch.common.exception.ErrorCode;
import io.github.kaltz.feldbuch.config.OpenAiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiKnowledgeSummaryService
        implements AiKnowledgeSummaryService {

    private static final String SUMMARY_LOG =
            "[AI_KNOWLEDGE_SUMMARY]";

    private static final int MIN_SUMMARY_LENGTH = 300;

    private static final int MIN_KEYWORD_COUNT = 3;

    private static final int MAX_KEYWORD_COUNT = 7;

    private static final String MARKDOWN_HEADING_PREFIX =
            "## ";

    private static final String CODE_FENCE =
            "```";

    private final AiClient aiClient;

    private final OpenAiProperties properties;

    private final ObjectMapper objectMapper;

    @Override
    public AiKnowledgeSummaryResponse summarize(
            String conversation
    ) {
        validateConversation(
                conversation
        );

        ChatCompletionRequest request =
                createRequest(
                        conversation
                );

        ChatCompletionResponse response =
                aiClient.chat(
                        request
                );

        String json =
                extractContent(
                        response
                );

        AiKnowledgeSummaryResponse summaryResponse =
                parseResponse(
                        json
                );

        validateResponse(
                summaryResponse
        );

        return summaryResponse;
    }

    private ChatCompletionRequest createRequest(
            String conversation
    ) {
        return new ChatCompletionRequest(
                properties.getModel(),
                List.of(
                        new Message(
                                "system",
                                KnowledgeSummaryPrompt
                                        .systemPrompt()
                        ),
                        new Message(
                                "user",
                                KnowledgeSummaryPrompt
                                        .userPrompt(
                                                conversation
                                        )
                        )
                )
        );
    }

    private AiKnowledgeSummaryResponse parseResponse(
            String json
    ) {
        try {
            return objectMapper.readValue(
                    json,
                    AiKnowledgeSummaryResponse.class
            );
        } catch (JsonProcessingException exception) {
            log.error(
                    "{} Failed to parse response. response={}",
                    SUMMARY_LOG,
                    json,
                    exception
            );

            throw new CustomException(
                    ErrorCode.OPENAI_SERVER_ERROR
            );
        }
    }

    private void validateResponse(
            AiKnowledgeSummaryResponse response
    ) {
        if (response == null) {
            throw invalidResponse(
                    "AI 지식 요약 응답이 없습니다."
            );
        }

        validateCategory(
                response
        );

        validateRequiredText(
                response.title(),
                "제목"
        );

        validateRequiredText(
                response.description(),
                "설명"
        );

        validateSummary(
                response.summary()
        );

        validateKeywords(
                response.keywords()
        );
    }

    private void validateCategory(
            AiKnowledgeSummaryResponse response
    ) {
        if (response.category() == null) {
            throw invalidResponse(
                    "AI 지식 카테고리가 없습니다."
            );
        }
    }

    private void validateSummary(
            String summary
    ) {
        validateRequiredText(
                summary,
                "요약"
        );

        String normalizedSummary =
                summary.trim();

        if (
                normalizedSummary.length()
                        < MIN_SUMMARY_LENGTH
        ) {
            throw invalidResponse(
                    "AI 지식 요약은 최소 "
                            + MIN_SUMMARY_LENGTH
                            + "자 이상이어야 합니다."
            );
        }

        validateMarkdownStructure(
                normalizedSummary
        );

        validateCodeFences(
                normalizedSummary
        );
    }

    /**
     * 학습 문서가 최소한 하나 이상의 Markdown 소제목을
     * 포함하는지 검증한다.
     */
    private void validateMarkdownStructure(
            String summary
    ) {
        boolean containsMarkdownHeading =
                summary.lines()
                        .map(String::trim)
                        .anyMatch(line ->
                                line.startsWith(
                                        MARKDOWN_HEADING_PREFIX
                                )
                        );

        if (!containsMarkdownHeading) {
            throw invalidResponse(
                    "AI 지식 요약에 Markdown 소제목이 없습니다."
            );
        }
    }

    /**
     * Markdown 코드 블록이 열렸다면 반드시 닫혀 있어야 한다.
     * <p>
     * 코드 블록이 없는 문서는 정상으로 처리한다.
     */
    private void validateCodeFences(
            String summary
    ) {
        int fenceCount =
                countOccurrences(
                        summary,
                        CODE_FENCE
                );

        if (fenceCount % 2 != 0) {
            throw invalidResponse(
                    "AI 지식 요약의 Markdown 코드 블록이 올바르게 닫히지 않았습니다."
            );
        }
    }

    private int countOccurrences(
            String text,
            String target
    ) {
        int count = 0;
        int index = 0;

        while (
                (
                        index =
                                text.indexOf(
                                        target,
                                        index
                                )
                ) >= 0
        ) {
            count++;
            index += target.length();
        }

        return count;
    }

    private void validateKeywords(
            List<String> keywords
    ) {
        if (keywords == null) {
            throw invalidResponse(
                    "AI 지식 키워드가 없습니다."
            );
        }

        if (
                keywords.size() < MIN_KEYWORD_COUNT
                        || keywords.size() > MAX_KEYWORD_COUNT
        ) {
            throw invalidResponse(
                    "AI 지식 키워드는 "
                            + MIN_KEYWORD_COUNT
                            + "개 이상 "
                            + MAX_KEYWORD_COUNT
                            + "개 이하이어야 합니다."
            );
        }

        boolean containsBlank =
                keywords.stream()
                        .anyMatch(keyword ->
                                keyword == null
                                        || keyword.isBlank()
                        );

        if (containsBlank) {
            throw invalidResponse(
                    "AI 지식 키워드에 빈 값이 포함되어 있습니다."
            );
        }

        validateDuplicateKeywords(
                keywords
        );
    }

    private void validateDuplicateKeywords(
            List<String> keywords
    ) {
        Set<String> normalizedKeywords =
                new HashSet<>();

        boolean containsDuplicate =
                keywords.stream()
                        .map(String::trim)
                        .map(keyword ->
                                keyword.toLowerCase(
                                        Locale.ROOT
                                )
                        )
                        .anyMatch(keyword ->
                                !normalizedKeywords.add(
                                        keyword
                                )
                        );

        if (containsDuplicate) {
            throw invalidResponse(
                    "AI 지식 키워드에 중복된 값이 포함되어 있습니다."
            );
        }
    }

    private void validateRequiredText(
            String value,
            String fieldName
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            throw invalidResponse(
                    "AI 지식 요약의 "
                            + fieldName
                            + "이 없습니다."
            );
        }
    }

    private void validateConversation(
            String conversation
    ) {
        if (
                conversation == null
                        || conversation.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "요약할 대화 내용은 필수입니다."
            );
        }
    }

    private CustomException invalidResponse(
            String message
    ) {
        log.warn(
                "{} Invalid response. reason={}",
                SUMMARY_LOG,
                message
        );

        return new CustomException(
                ErrorCode.OPENAI_SERVER_ERROR
        );
    }

    private static String extractContent(
            ChatCompletionResponse response
    ) {
        if (
                response == null
                        || response.choices() == null
                        || response.choices().isEmpty()
        ) {
            throw new CustomException(
                    ErrorCode.OPENAI_SERVER_ERROR
            );
        }

        if (
                response
                        .choices()
                        .getFirst()
                        .message() == null
        ) {
            throw new CustomException(
                    ErrorCode.OPENAI_SERVER_ERROR
            );
        }

        String content =
                response
                        .choices()
                        .getFirst()
                        .message()
                        .content();

        if (
                content == null
                        || content.isBlank()
        ) {
            throw new CustomException(
                    ErrorCode.OPENAI_SERVER_ERROR
            );
        }

        return content.trim();
    }
}