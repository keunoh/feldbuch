package io.github.kaltz.feldbuch.ai.dto.openai;

import java.util.List;

public record ChatCompletionStreamRequest(
        String model,
        List<Message> messages,
        boolean stream
) {

    public static ChatCompletionStreamRequest from(
            ChatCompletionRequest request
    ) {
        return new ChatCompletionStreamRequest(
                request.model(),
                request.messages(),
                true
        );
    }
}
