package io.github.kaltz.feldbuch.ai.controller;

import io.github.kaltz.feldbuch.ai.model.ChatResponse;
import io.github.kaltz.feldbuch.auth.security.CustomUserDetails;
import io.github.kaltz.feldbuch.common.response.ApiResponse;
import io.github.kaltz.feldbuch.conversation.dto.request.ChatRequest;
import io.github.kaltz.feldbuch.conversation.dto.response.StreamResponse;
import io.github.kaltz.feldbuch.conversation.service.ConversationChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

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

    /**
     * SSE 스트리밍 응답은 이벤트 단위 계약인 StreamResponse를 사용한다.
     * 일반 JSON 응답용 ApiResponse로 감싸지 않는다.
     *
     */
    @PostMapping(
            value = "/{conversationId}/chat/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<StreamResponse> stream(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long conversationId,
            @Valid @RequestBody ChatRequest request
    ) {
        return conversationChatService.stream(
                userDetails.getUserId(),
                conversationId,
                request
        );
    }

}
