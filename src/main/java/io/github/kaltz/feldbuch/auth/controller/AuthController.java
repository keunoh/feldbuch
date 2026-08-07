package io.github.kaltz.feldbuch.auth.controller;

import io.github.kaltz.feldbuch.auth.dto.request.LoginRequest;
import io.github.kaltz.feldbuch.auth.dto.response.AuthMeResponse;
import io.github.kaltz.feldbuch.auth.dto.response.LoginResponse;
import io.github.kaltz.feldbuch.auth.security.CustomUserDetails;
import io.github.kaltz.feldbuch.auth.service.AuthService;
import io.github.kaltz.feldbuch.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        return ApiResponse.success(
                authService.login(request)
        );
    }

    @GetMapping("/me")
    public ApiResponse<AuthMeResponse> me(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        return ApiResponse.success(
                new AuthMeResponse(
                        userDetails.getUserId(),
                        userDetails.getUsername(),
                        userDetails.getUser().getNickname(),
                        userDetails.getUser().getRole().name()
                )
        );
    }
}
