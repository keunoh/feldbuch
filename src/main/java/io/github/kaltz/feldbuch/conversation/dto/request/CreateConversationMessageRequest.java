package io.github.kaltz.feldbuch.conversation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateConversationMessageRequest(
        @NotBlank(message = "메시지는 비어 있을 수 있습니다.")
        String content
) {
}
