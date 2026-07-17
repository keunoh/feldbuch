package io.github.kaltz.feldbuch.conversation.dto.response;

import io.github.kaltz.feldbuch.conversation.entity.ConversationMessage;
import io.github.kaltz.feldbuch.conversation.entity.ConversationRole;

import java.time.LocalDateTime;

public record ConversationMessageResponse(
        Long id,
        ConversationRole role,
        String content,
        LocalDateTime createdAt
) {

    public static ConversationMessageResponse from(ConversationMessage message) {

        return new ConversationMessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
