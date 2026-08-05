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
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeRootCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiKnowledgeSummaryService implements AiKnowledgeSummaryService {

    private static final String SUMMARY_LOG = "[AI_KNOWLEDGE_SUMMARY]";

    private static final int MAX_KNOWLEDGE_PATH_DEPTH = 2;

    private static final int MIN_KEYWORD_COUNT = 3;
    private static final int MAX_KEYWORD_COUNT = 7;

    private final AiClient aiClient;

    private final OpenAiProperties properties;

    private final ObjectMapper objectMapper;

    @Override
    public AiKnowledgeSummaryResponse summarize(String conversation) {

        ChatCompletionRequest request =
                createRequest(conversation);

        ChatCompletionResponse response =
                aiClient.chat(request);

        String json =
                extractContent(response);

        AiKnowledgeSummaryResponse summaryResponse =
                parseResponse(json);

        validateResponse(summaryResponse);

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

        if (response.rootCategory() == null) {
            throw invalidResponse(
                    "AI 지식 요약의 대분류가 없습니다."
            );
        }

        validateKnowledgePath(
                response.rootCategory(),
                response.knowledgePath()
        );

        validateRequiredText(
                response.title(),
                "제목"
        );

        validateRequiredText(
                response.description(),
                "설명"
        );

        validateRequiredText(
                response.summary(),
                "요약"
        );

        validateKeywords(
                response.keywords()
        );
    }

    private void validateKnowledgePath(
            KnowledgeRootCategory rootCategory,
            List<String> knowledgePath
    ) {
        if (
                knowledgePath == null
                        || knowledgePath.isEmpty()
        ) {
            throw invalidResponse(
                    "AI 지식 경로가 없습니다."
            );
        }

        if (
                knowledgePath.size()
                        > MAX_KNOWLEDGE_PATH_DEPTH
        ) {
            throw invalidResponse(
                    "AI 지식 하위 경로는 최대 "
                            + MAX_KNOWLEDGE_PATH_DEPTH
                            + "단계까지 허용됩니다."
            );
        }

        boolean containsBlank =
                knowledgePath.stream()
                        .anyMatch(path ->
                                path == null
                                        || path.isBlank()
                        );

        if (containsBlank) {
            throw invalidResponse(
                    "AI 지식 경로에 빈 값이 포함되어 있습니다."
            );
        }

        boolean containsRootCategory =
                knowledgePath.stream()
                        .map(String::trim)
                        .anyMatch(path ->
                                path.equalsIgnoreCase(
                                        rootCategory.name()
                                )
                                        || path.equalsIgnoreCase(
                                        rootCategory
                                                .getDisplayName()
                                )
                        );

        if (containsRootCategory) {
            throw invalidResponse(
                    "AI 지식 경로에 대분류가 중복되어 있습니다."
            );
        }
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
