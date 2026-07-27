package io.github.kaltz.feldbuch.conversation.dto.response;

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
}
