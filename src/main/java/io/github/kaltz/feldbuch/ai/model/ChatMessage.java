package io.github.kaltz.feldbuch.ai.model;

public record ChatMessage(
        ChatRole role,
        String content
) {
}
