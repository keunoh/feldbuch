package io.github.kaltz.feldbuch.ai.controller;


import io.github.kaltz.feldbuch.ai.facade.AiFacade;
import io.github.kaltz.feldbuch.auth.security.CustomUserDetails;
import io.github.kaltz.feldbuch.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiFacade aiFacade;

    @PostMapping("/notes/{noteId}/summary")
    public ApiResponse<Void> summarize(
            @AuthenticationPrincipal
            CustomUserDetails user,

            @PathVariable
            Long noteId
    ) {
        aiFacade.summarize(
                user.getUserId(),
                noteId
        );

        return ApiResponse.success(null);
    }
}
