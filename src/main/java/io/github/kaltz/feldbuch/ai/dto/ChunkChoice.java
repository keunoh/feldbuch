package io.github.kaltz.feldbuch.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChunkChoice(
        int index,
        Delta delta,

        @JsonProperty("finish_reason")
        String finishReason
) {
}
