package io.github.kaltz.feldbuch.user.dto;

public record UserMeResponse(
        Long id,
        String email,
        String nickname,
        String role
) {
}
