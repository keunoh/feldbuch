package io.github.kaltz.feldbuch.user.dto;

public record SignupResponse(
        Long id,
        String email,
        String nickname
) {
}
