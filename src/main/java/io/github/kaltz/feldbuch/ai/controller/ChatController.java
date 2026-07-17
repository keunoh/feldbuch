package io.github.kaltz.feldbuch.ai.controller;

import io.github.kaltz.feldbuch.ai.dto.ChatRequest;
import io.github.kaltz.feldbuch.ai.model.ChatResponse;
import io.github.kaltz.feldbuch.auth.security.CustomUserDetails;
import io.github.kaltz.feldbuch.common.response.ApiResponse;
import io.github.kaltz.feldbuch.conversation.service.ConversationChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/conversations")
public class ChatController {

    private final ConversationChatService conversationChatService;

    @PostMapping("/{conversationId}/chat")
    public ApiResponse<ChatResponse> chat(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long conversationId,
            @Valid @RequestBody ChatRequest request
    ) {

        return ApiResponse.success(
                conversationChatService.chat(
                        user.getUserId(),
                        conversationId,
                        request
                )
        );
    }
}
