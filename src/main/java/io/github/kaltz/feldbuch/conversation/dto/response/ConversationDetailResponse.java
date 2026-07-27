package io.github.kaltz.feldbuch.conversation.dto.response;

import io.github.kaltz.feldbuch.conversation.entity.Conversation;

import java.time.LocalDateTime;
import java.util.List;

public record ConversationDetailResponse(
        Long id,
        String title,
        String status,
        LocalDateTime createdAt,
        List<ConversationMessageResponse> messages,
        Long messageCount
) {

    public static ConversationDetailResponse from(
            Conversation conversation,
            List<ConversationMessageResponse> messages
    ) {
        return new ConversationDetailResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getStatus().name(),
                conversation.getCreatedAt(),
                messages,
                (long) messages.size()
        );
    }
}
