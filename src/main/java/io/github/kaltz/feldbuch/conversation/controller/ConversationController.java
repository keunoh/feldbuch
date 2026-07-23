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

import java.util.List;

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
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long conversationId
    ) {

        return ApiResponse.success(
                queryService.findById(user.getUserId(), conversationId)
        );
    }

    @GetMapping
    public ApiResponse<List<ConversationResponse>> findAll11(
            @AuthenticationPrincipal CustomUserDetails user
    ) {

        return ApiResponse.success(
                queryService.findAll(user.getUserId())
        );
    }
}
