package io.github.kaltz.feldbuch.conversation.controller;

import io.github.kaltz.feldbuch.auth.security.CustomUserDetails;
import io.github.kaltz.feldbuch.common.response.ApiResponse;
import io.github.kaltz.feldbuch.conversation.dto.request.CreateConversationMessageRequest;
import io.github.kaltz.feldbuch.conversation.dto.response.ConversationMessageResponse;
import io.github.kaltz.feldbuch.conversation.service.ConversationMessageCommandService;
import io.github.kaltz.feldbuch.conversation.service.ConversationQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations/{conversationId}/messages")
@RequiredArgsConstructor
public class ConversationMessageController {

    private final ConversationMessageCommandService commandService;
    private final ConversationQueryService queryService;

    @PostMapping
    public ApiResponse<Long> create(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long conversationId,
            @Valid @RequestBody CreateConversationMessageRequest request
    ) {

        Long messageId = commandService.create(user.getUserId(), conversationId, request);

        return ApiResponse.success(messageId);
    }

    @GetMapping
    public ApiResponse<List<ConversationMessageResponse>> findAll(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long conversationId
    ) {

        return ApiResponse.success(
                queryService.findAll(user.getUserId(), conversationId)
        );
    }
}
