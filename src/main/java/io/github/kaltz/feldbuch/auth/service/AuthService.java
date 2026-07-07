package io.github.kaltz.feldbuch.auth.service;

import io.github.kaltz.feldbuch.auth.dto.request.LoginRequest;
import io.github.kaltz.feldbuch.auth.dto.response.LoginResponse;
import io.github.kaltz.feldbuch.auth.jwt.JwtProvider;
import io.github.kaltz.feldbuch.auth.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    public LoginResponse login(LoginRequest request) {

        Authentication authentication =
                // 이 한 줄이 Spring Security 전체를 움직인다.
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.email(),
                                request.password()
                        )
                );

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        String accessToken =
                jwtProvider.createAccessToken(
                        userDetails.getUserId(),
                        userDetails.getUsername(),
                        userDetails.getUser().getRole().name()
                );

        return new LoginResponse(
                userDetails.getUserId(),
                accessToken,
                "Bearer"
        );
    }
}
