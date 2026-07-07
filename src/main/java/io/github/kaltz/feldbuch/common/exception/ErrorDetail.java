package io.github.kaltz.feldbuch.common.exception;

import lombok.Builder;

@Builder
public record ErrorDetail(
        String code,
        String message
) {
}
