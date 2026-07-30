package io.github.kaltz.feldbuch.ai.client;

import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionRequest;
import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionResponse;
import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionStreamRequest;
import io.github.kaltz.feldbuch.common.exception.CustomException;
import io.github.kaltz.feldbuch.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class OpenAiClient {

    private static final String STREAM_DONE = "[DONE]";

    private final RestClient openAiRestClient;
    private final WebClient openAiWebClient;

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

    public Flux<String> stream(ChatCompletionRequest request) {

        ChatCompletionStreamRequest streamRequest =
                ChatCompletionStreamRequest.from(request);

        return openAiWebClient.post()
                .uri("/chat/completions")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(streamRequest)
                .retrieve()
                .onStatus(
                        status -> status.value() == 401,
                        response -> Mono.error(
                                new CustomException(
                                        ErrorCode.OPENAI_UNAUTHORIZED
                                )
                        )
                )
                .onStatus(
                        status -> status.value() == 429,
                        response -> Mono.error(
                                new CustomException(
                                        ErrorCode.OPENAI_RATE_LIMIT
                                )
                        )
                )
                .onStatus(
                        status -> status.is5xxServerError(),
                        response -> Mono.error(
                                new CustomException(
                                        ErrorCode.OPENAI_SERVER_ERROR
                                )
                        )
                )
                .bodyToFlux(String.class)
                .filter(data -> !data.isBlank())
                .takeUntil(STREAM_DONE::equals)
                .filter(data -> !STREAM_DONE.equals(data))
                .onErrorMap(
                        WebClientRequestException.class,
                        exception -> new CustomException(
                                ErrorCode.OPENAI_TIMEOUT
                        )
                );
    }
}
