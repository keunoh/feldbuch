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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiKnowledgeSummaryService implements AiKnowledgeSummaryService {

    private final AiClient aiClient;

    private final OpenAiProperties properties;

    private final ObjectMapper objectMapper;

    @Override
    public AiKnowledgeSummaryResponse summarize(String conversation) {

        ChatCompletionRequest request =
                new ChatCompletionRequest(
                        properties.getModel(),
                        List.of(
                                new Message(
                                        "system",
                                        KnowledgeSummaryPrompt.systemPrompt()
                                ),
                                new Message(
                                        "user",
                                        KnowledgeSummaryPrompt.userPrompt(
                                                conversation
                                        )
                                )
                        )
                );

        ChatCompletionResponse response =
                aiClient.chat(request);

        String json =
                extractContent(response);

        try {

            return objectMapper.readValue(
                    json,
                    AiKnowledgeSummaryResponse.class
            );

        } catch (JsonProcessingException e) {

            throw new CustomException(
                    ErrorCode.OPENAI_SERVER_ERROR
            );
        } catch (Exception e) {
            log.error("OpenAI 호출 실패", e);

            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static String extractContent(ChatCompletionResponse response) {

        if (response.choices().isEmpty()) {
            throw new CustomException(
                    ErrorCode.OPENAI_SERVER_ERROR
            );
        }

        return response
                .choices()
                .getFirst()
                .message()
                .content();
    }
}
