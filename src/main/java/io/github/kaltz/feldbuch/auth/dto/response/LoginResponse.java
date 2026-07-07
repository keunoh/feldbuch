package io.github.kaltz.feldbuch.auth.dto.response;

public record LoginResponse(
        Long userId,
        String accessToken,
        String tokenType
) {
}
