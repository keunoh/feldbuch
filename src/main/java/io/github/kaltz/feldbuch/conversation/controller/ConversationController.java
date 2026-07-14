package io.github.kaltz.feldbuch.conversation.controller;

import io.github.kaltz.feldbuch.auth.security.CustomUserDetails;
import io.github.kaltz.feldbuch.common.response.ApiResponse;
import io.github.kaltz.feldbuch.conversation.dto.request.CreateConversationRequest;
import io.github.kaltz.feldbuch.conversation.dto.response.ConversationResponse;
import io.github.kaltz.feldbuch.conversation.service.ConversationCommandService;
import io.github.kaltz.feldbuch.conversation.service.ConversationQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationCommandService commandService;
    private final ConversationQueryService queryService;

    @PostMapping
    public ApiResponse<Long> create(
            @AuthenticationPrincipal CustomUserDetails user,

            @Valid @RequestBody
            CreateConversationRequest request
    ) {

        return ApiResponse.success(
                commandService.create(user.getUserId(), request)
        );
    }

    @GetMapping("/{conversationId}")
    public ApiResponse<ConversationResponse> findById(
            @PathVariable
            Long conversationId
    ) {

        return ApiResponse.success(
                queryService.findById(conversationId)
        );
    }
}
