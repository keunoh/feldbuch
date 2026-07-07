package io.github.kaltz.feldbuch.user.controller;

import io.github.kaltz.feldbuch.auth.security.CustomUserDetails;
import io.github.kaltz.feldbuch.common.response.ApiResponse;
import io.github.kaltz.feldbuch.user.dto.SignupRequest;
import io.github.kaltz.feldbuch.user.dto.SignupResponse;
import io.github.kaltz.feldbuch.user.dto.UserMeResponse;
import io.github.kaltz.feldbuch.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SignupResponse> signup(
            @RequestBody
            // @RequestBody : JSON > Java 객체 자동 변환
            // SignupRequest로 바뀐다.
            // Jackson이 담당한다.
            @Valid
            // Validation 시작
            // 검사 실패하면 MethodArgumentNotValidException 발생
            // GlobalExceptionHandler
            SignupRequest request
    ) {
        return ApiResponse.success(
                userService.signup(request)
        );
    }

    @GetMapping("/me")
    public ApiResponse<UserMeResponse> me(
            @AuthenticationPrincipal CustomUserDetails user
    ) {

        return ApiResponse.success(
                userService.getMyInfo(user.getUserId())
        );
    }
}
