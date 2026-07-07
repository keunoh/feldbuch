package io.github.kaltz.feldbuch.common.exception;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String code,
        String message
) {
}
