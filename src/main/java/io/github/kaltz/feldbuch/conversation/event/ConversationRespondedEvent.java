package io.github.kaltz.feldbuch.conversation.event;

public record ConversationRespondedEvent(
        Long userId,
        Long conversationId,
        String userMessage
) {
}
