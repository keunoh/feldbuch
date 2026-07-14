package io.github.kaltz.feldbuch.conversation.dto.response;

import io.github.kaltz.feldbuch.conversation.entity.Conversation;

import java.time.LocalDateTime;

public record ConversationResponse(
        Long id,
        String title,
        String status,
        LocalDateTime createdAt
) {

    public static ConversationResponse from(Conversation conversation) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getStatus().name(),
                conversation.getCreatedAt()
        );
    }
}
