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

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiKnowledgeMergeService
        implements AiKnowledgeMergeService {

    private static final String MERGE_LOG =
            "[AI_KNOWLEDGE_MERGE]";

    /**
     * 통합 노트는 기존 내용과 신규 내용을 함께 담아야 하므로
     * 증분 노트보다 더 긴 최소 길이를 요구한다.
     */
    private static final int MIN_SUMMARY_LENGTH =
            500;

    private static final int MIN_KEYWORD_COUNT =
            3;

    private static final int MAX_KEYWORD_COUNT =
            7;

    private static final String MARKDOWN_HEADING_PREFIX =
            "## ";

    private static final String CODE_FENCE =
            "```";

    private final AiClient aiClient;

    private final OpenAiProperties properties;

    private final ObjectMapper objectMapper;

    /**
     * 기존 통합 노트와 새로운 증분 노트를
     * 하나의 Markdown 통합 학습 문서로 병합한다.
     */
    @Override
    public AiKnowledgeMergeResponse merge(
            KnowledgeNote consolidatedNote,
            KnowledgeNote incrementalNote
    ) {
        validateNotes(
                consolidatedNote,
                incrementalNote
        );

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

    /**
     * AI 요청 메시지를 생성한다.
     */
    private ChatCompletionRequest createRequest(
            KnowledgeNote consolidatedNote,
            KnowledgeNote incrementalNote
    ) {
        return new ChatCompletionRequest(
                properties.getModel(),
                List.of(
                        new Message(
                                "system",
                                KnowledgeMergePrompt
                                        .systemPrompt()
                        ),
                        new Message(
                                "user",
                                KnowledgeMergePrompt
                                        .userPrompt(
                                                consolidatedNote,
                                                incrementalNote
                                        )
                        )
                )
        );
    }

    /**
     * AI가 반환한 JSON 문자열을 DTO로 변환한다.
     */
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

    /**
     * AI 병합 응답의 전체 형식을 검증한다.
     */
    private void validateResponse(
            AiKnowledgeMergeResponse response
    ) {
        if (response == null) {
            throw invalidResponse(
                    "AI 지식 병합 응답이 없습니다."
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
            AiKnowledgeMergeResponse response
    ) {
        if (response.category() == null) {
            throw invalidResponse(
                    "AI 지식 병합 카테고리가 없습니다."
            );
        }
    }

    /**
     * 통합 Markdown 문서를 검증한다.
     */
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
                    "AI 지식 병합 요약은 최소 "
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
     * 통합 문서에 최소 하나 이상의
     * Markdown 소제목이 있는지 확인한다.
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
                    "AI 지식 병합 요약에 Markdown 소제목이 없습니다."
            );
        }
    }

    /**
     * Markdown 코드 블록이 열렸다면
     * 반드시 정상적으로 닫혀 있어야 한다.
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
                    "AI 지식 병합 요약의 Markdown 코드 블록이 "
                            + "올바르게 닫히지 않았습니다."
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

    /**
     * 검색 키워드 개수, 빈 값, 중복 여부를 검증한다.
     */
    private void validateKeywords(
            List<String> keywords
    ) {
        if (keywords == null) {
            throw invalidResponse(
                    "AI 지식 병합 키워드가 없습니다."
            );
        }

        if (
                keywords.size() < MIN_KEYWORD_COUNT
                        || keywords.size() > MAX_KEYWORD_COUNT
        ) {
            throw invalidResponse(
                    "AI 지식 병합 키워드는 "
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
                    "AI 지식 병합 키워드에 "
                            + "빈 값이 포함되어 있습니다."
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
                    "AI 지식 병합 키워드에 "
                            + "중복된 값이 포함되어 있습니다."
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
                    "AI 지식 병합 응답의 "
                            + fieldName
                            + "이 없습니다."
            );
        }
    }

    /**
     * 병합 입력 노트의 null 여부와 유형을 검증한다.
     */
    private void validateNotes(
            KnowledgeNote consolidatedNote,
            KnowledgeNote incrementalNote
    ) {
        if (consolidatedNote == null) {
            throw new IllegalArgumentException(
                    "통합 KnowledgeNote는 필수입니다."
            );
        }

        if (!consolidatedNote.isConsolidated()) {
            throw new IllegalArgumentException(
                    "통합 노트 유형만 기존 노트로 사용할 수 있습니다."
            );
        }

        if (incrementalNote == null) {
            throw new IllegalArgumentException(
                    "증분 KnowledgeNote는 필수입니다."
            );
        }

        if (!incrementalNote.isIncremental()) {
            throw new IllegalArgumentException(
                    "증분 노트 유형만 신규 노트로 사용할 수 있습니다."
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

    /**
     * OpenAI 응답 객체에서 실제 JSON 내용을 추출한다.
     */
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