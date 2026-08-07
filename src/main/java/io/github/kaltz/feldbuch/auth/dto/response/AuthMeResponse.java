package io.github.kaltz.feldbuch.auth.dto.response;

public record AuthMeResponse(
        Long userId,
        String email,
        String nickname,
        String role
) {
}
