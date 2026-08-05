package io.github.kaltz.feldbuch.knowledge.folder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kaltz.feldbuch.ai.client.AiClient;
import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionRequest;
import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionResponse;
import io.github.kaltz.feldbuch.ai.dto.openai.Message;
import io.github.kaltz.feldbuch.ai.prompt.KnowledgeFolderSelectionPrompt;
import io.github.kaltz.feldbuch.common.exception.CustomException;
import io.github.kaltz.feldbuch.common.exception.ErrorCode;
import io.github.kaltz.feldbuch.config.OpenAiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiKnowledgeFolderSelectionService implements KnowledgeFolderSelectionService {

    private static final String SELECTION_LOG =
            "[AI_KNOWLEDGE_FOLDER_SELECTION]";

    private final AiClient aiClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public AiKnowledgeFolderSelectionResponse select(
            String rootCategoryName,
            String parentFolderName,
            String requestedFolderName,
            List<KnowledgeFolderCandidate> candidates
    ) {
        List<KnowledgeFolderCandidate> safeCandidates =
                candidates == null
                        ? List.of()
                        : List.copyOf(candidates);

        /*
         * 후보가 없으면 AI를 호출할 이유가 없다.
         */
        if (safeCandidates.isEmpty()) {
            return createSelection();
        }

        ChatCompletionRequest request =
                new ChatCompletionRequest(
                        properties.getModel(),
                        List.of(
                                new Message(
                                        "system",
                                        KnowledgeFolderSelectionPrompt
                                                .systemPrompt()
                                ),
                                new Message(
                                        "user",
                                        KnowledgeFolderSelectionPrompt
                                                .userPrompt(
                                                        rootCategoryName,
                                                        parentFolderName,
                                                        requestedFolderName,
                                                        safeCandidates
                                                )
                                )
                        )
                );

        ChatCompletionResponse chatResponse =
                aiClient.chat(request);

        String json =
                extractContent(chatResponse);

        AiKnowledgeFolderSelectionResponse response =
                parseResponse(json);

        validateResponse(
                response,
                safeCandidates
        );

        return response;
    }

    private AiKnowledgeFolderSelectionResponse parseResponse(
            String json
    ) {
        try {
            return objectMapper.readValue(
                    json,
                    AiKnowledgeFolderSelectionResponse.class
            );
        } catch (JsonProcessingException exception) {
            log.error(
                    "{} Failed to parse response. response={}",
                    SELECTION_LOG,
                    json,
                    exception
            );

            throw new CustomException(
                    ErrorCode.OPENAI_SERVER_ERROR
            );
        }
    }

    private void validateResponse(
            AiKnowledgeFolderSelectionResponse response,
            List<KnowledgeFolderCandidate> candidates
    ) {
        if (
                response == null
                        || response.selectionType() == null
        ) {
            throw invalidResponse(
                    "폴더 선택 유형이 없습니다."
            );
        }

        if (
                response.selectionType()
                        == AiKnowledgeFolderSelectionType.CREATE
        ) {
            if (response.selectedKnowledgeId() != null) {
                throw invalidResponse(
                        "CREATE 응답에는 기존 폴더 ID가 포함될 수 없습니다."
                );
            }

            return;
        }

        Long selectedKnowledgeId =
                response.selectedKnowledgeId();

        if (selectedKnowledgeId == null) {
            throw invalidResponse(
                    "EXISTING 응답에는 폴더 ID가 필요합니다."
            );
        }

        Set<Long> candidateIds =
                candidates.stream()
                        .map(
                                KnowledgeFolderCandidate::id
                        )
                        .collect(
                                Collectors.toSet()
                        );

        if (
                !candidateIds.contains(
                        selectedKnowledgeId
                )
        ) {
            throw invalidResponse(
                    "AI가 후보 목록에 없는 폴더를 선택했습니다."
            );
        }
    }

    private AiKnowledgeFolderSelectionResponse createSelection() {
        return new AiKnowledgeFolderSelectionResponse(
                AiKnowledgeFolderSelectionType.CREATE,
                null
        );
    }

    private CustomException invalidResponse(
            String reason
    ) {
        log.warn(
                "{} Invalid response. reason={}",
                SELECTION_LOG,
                reason
        );

        return new CustomException(
                ErrorCode.OPENAI_SERVER_ERROR
        );
    }

    private String extractContent(
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
