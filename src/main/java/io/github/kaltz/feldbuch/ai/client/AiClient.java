package io.github.kaltz.feldbuch.ai.client;

import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionRequest;
import io.github.kaltz.feldbuch.ai.dto.openai.ChatCompletionResponse;
import reactor.core.publisher.Flux;

public interface AiClient {

    ChatCompletionResponse chat(
            ChatCompletionRequest request
    );

    Flux<String> stream(
            ChatCompletionRequest request
    );
}
