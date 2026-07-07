package io.github.kaltz.feldbuch.auth.controller;

import io.github.kaltz.feldbuch.auth.dto.request.LoginRequest;
import io.github.kaltz.feldbuch.auth.dto.response.LoginResponse;
import io.github.kaltz.feldbuch.auth.service.AuthService;
import io.github.kaltz.feldbuch.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
