package io.github.kaltz.feldbuch.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kaltz.feldbuch.ai.client.AiClient;
import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeMergeResponse;
import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionRequest;
import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionResponse;
import io.github.kaltz.feldbuch.ai.dto.openai.Message;
import io.github.kaltz.feldbuch.ai.prompt.KnowledgeMergePrompt;
import io.github.kaltz.feldbuch.common.exception.CustomException;
import io.github.kaltz.feldbuch.common.exception.ErrorCode;
import io.github.kaltz.feldbuch.config.OpenAiProperties;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiKnowledgeMergeService
        implements AiKnowledgeMergeService {

    private static final String MERGE_LOG =
            "[AI_KNOWLEDGE_MERGE]";

    private static final int MIN_KEYWORD_COUNT = 3;

    private static final int MAX_KEYWORD_COUNT = 7;

    private final AiClient aiClient;

    private final OpenAiProperties properties;

    private final ObjectMapper objectMapper;

    @Override
    public AiKnowledgeMergeResponse merge(
            KnowledgeNote consolidatedNote,
            KnowledgeNote incrementalNote
    ) {

        ChatCompletionRequest request =
                createRequest(
                        consolidatedNote,
                        incrementalNote
                );

        ChatCompletionResponse response =
                aiClient.chat(
                        request
                );

        String json =
                extractContent(
                        response
                );

        AiKnowledgeMergeResponse mergeResponse =
                parseResponse(
                        json
                );

        validateResponse(
                mergeResponse
        );

        return mergeResponse;
    }

    private ChatCompletionRequest createRequest(
            KnowledgeNote consolidatedNote,
            KnowledgeNote incrementalNote
    ) {

        return new ChatCompletionRequest(
                properties.getModel(),
                List.of(
                        new Message(
                                "system",
                                KnowledgeMergePrompt.systemPrompt()
                        ),
                        new Message(
                                "user",
                                KnowledgeMergePrompt.userPrompt(
                                        consolidatedNote,
                                        incrementalNote
                                )
                        )
                )
        );
    }

    private AiKnowledgeMergeResponse parseResponse(
            String json
    ) {

        try {
            return objectMapper.readValue(
                    json,
                    AiKnowledgeMergeResponse.class
            );
        } catch (JsonProcessingException exception) {

            log.error(
                    "{} Failed to parse response. response={}",
                    MERGE_LOG,
                    json,
                    exception
            );

            throw new CustomException(
                    ErrorCode.OPENAI_SERVER_ERROR
            );
        }
    }

    private void validateResponse(
            AiKnowledgeMergeResponse response
    ) {

        if (response == null) {
            throw invalidResponse(
                    "AI 병합 응답이 없습니다."
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

        validateRequiredText(
                response.summary(),
                "요약"
        );

        validateKeywords(
                response.keywords()
        );
    }

    private void validateCategory(
            AiKnowledgeMergeResponse response
    ) {

        if (response.category() == null) {
            throw invalidResponse(
                    "AI 병합 카테고리가 없습니다."
            );
        }
    }

    private void validateKeywords(
            List<String> keywords
    ) {

        if (keywords == null) {
            throw invalidResponse(
                    "AI 키워드가 없습니다."
            );
        }

        if (
                keywords.size() < MIN_KEYWORD_COUNT
                        || keywords.size() > MAX_KEYWORD_COUNT
        ) {
            throw invalidResponse(
                    "AI 키워드는 "
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
                    "AI 키워드에 빈 값이 포함되어 있습니다."
            );
        }

        long distinctCount =
                keywords.stream()
                        .map(String::trim)
                        .distinct()
                        .count();

        if (distinctCount != keywords.size()) {
            throw invalidResponse(
                    "AI 키워드에 중복된 값이 포함되어 있습니다."
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
                    "AI 병합 결과의 "
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
                MERGE_LOG,
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

        Message message =
                response
                        .choices()
                        .getFirst()
                        .message();

        if (message == null) {
            throw new CustomException(
                    ErrorCode.OPENAI_SERVER_ERROR
            );
        }

        String content =
                message.content();

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