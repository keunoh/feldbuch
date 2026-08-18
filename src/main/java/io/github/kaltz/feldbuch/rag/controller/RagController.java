package io.github.kaltz.feldbuch.rag.controller;

import io.github.kaltz.feldbuch.ai.model.ChatResponse;
import io.github.kaltz.feldbuch.auth.security.CustomUserDetails;
import io.github.kaltz.feldbuch.common.response.ApiResponse;
import io.github.kaltz.feldbuch.rag.dto.RagAnswerRequest;
import io.github.kaltz.feldbuch.rag.dto.RagAnswerResponse;
import io.github.kaltz.feldbuch.rag.service.RagAnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagAnswerService ragAnswerService;

    @PostMapping("/answer")
    public ApiResponse<RagAnswerResponse> answer(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody RagAnswerRequest request
    ) {

        ChatResponse response = ragAnswerService.answer(
                user.getUserId(),
                request.question()
        );

        return ApiResponse.success(
                RagAnswerResponse.from(
                        response.content()
                )
        );
    }
}
