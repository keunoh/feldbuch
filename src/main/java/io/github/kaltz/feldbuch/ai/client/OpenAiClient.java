package io.github.kaltz.feldbuch.ai.client;

import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionRequest;
import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionResponse;
import io.github.kaltz.feldbuch.common.exception.CustomException;
import io.github.kaltz.feldbuch.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class OpenAiClient {

    private final RestClient openAiRestClient;

    public ChatCompletionResponse chat(ChatCompletionRequest request) {

        try {
            return openAiRestClient.post()
                    .uri("/chat/completions")
                    .body(request)
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 401,
                            (req, res) -> {
                                throw new CustomException(ErrorCode.OPENAI_UNAUTHORIZED);
                            }
                    )
                    .onStatus(
                            status -> status.value() == 429,
                            (req, res) -> {
                                throw new CustomException(ErrorCode.OPENAI_RATE_LIMIT);
                            }
                    )
                    .onStatus(
                            status -> status.is5xxServerError(),
                            (req, res) -> {
                                throw new CustomException(ErrorCode.OPENAI_SERVER_ERROR);
                            }
                    )
                    .body(ChatCompletionResponse.class);
        } catch (ResourceAccessException e) {
            throw new CustomException(ErrorCode.OPENAI_TIMEOUT);
        }
    }
}
