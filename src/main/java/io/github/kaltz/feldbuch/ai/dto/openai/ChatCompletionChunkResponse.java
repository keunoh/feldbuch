package io.github.kaltz.feldbuch.ai.dto.openai;

import io.github.kaltz.feldbuch.ai.dto.ChunkChoice;
import io.github.kaltz.feldbuch.ai.dto.Delta;

import java.util.List;

public record ChatCompletionChunkResponse(
        List<ChunkChoice> choices
) {

    public String content() {
        if (choices == null || choices.isEmpty()) {
            return null;
        }

        Delta delta = choices.getFirst().delta();

        if (delta == null) {
            return null;
        }

        return delta.content();
    }
}
