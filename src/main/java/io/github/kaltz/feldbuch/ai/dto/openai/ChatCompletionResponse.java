package io.github.kaltz.feldbuch.ai.dto.openai;

import java.util.List;

public record ChatCompletionResponse(
        List<Choice> choices
) {
}
