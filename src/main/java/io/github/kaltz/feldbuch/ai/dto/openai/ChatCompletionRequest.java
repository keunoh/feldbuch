package io.github.kaltz.feldbuch.ai.dto.openai;

import java.util.List;

public record ChatCompletionRequest(
        String model,
        List<Message> messages
) {
}
