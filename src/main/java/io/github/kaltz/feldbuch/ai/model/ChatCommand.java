package io.github.kaltz.feldbuch.ai.model;

import java.util.List;

public record ChatCommand(
        List<ChatMessage> messages
) {

    public static ChatCommand from(List<ChatMessage> messages) {
        return new ChatCommand(List.copyOf(messages));
    }
}
