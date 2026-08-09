package io.github.kaltz.feldbuch.auth.dto.response;

public record RefreshTokenResponse(
        String accessToken,
        String tokenType
) {
}