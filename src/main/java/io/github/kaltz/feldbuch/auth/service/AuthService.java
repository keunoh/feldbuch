package io.github.kaltz.feldbuch.auth.service;

import io.github.kaltz.feldbuch.auth.dto.request.LoginRequest;
import io.github.kaltz.feldbuch.auth.dto.request.RefreshTokenRequest;
import io.github.kaltz.feldbuch.auth.dto.response.AuthMeResponse;
import io.github.kaltz.feldbuch.auth.dto.response.LoginResponse;
import io.github.kaltz.feldbuch.auth.dto.response.RefreshTokenResponse;
import io.github.kaltz.feldbuch.auth.jwt.JwtProvider;
import io.github.kaltz.feldbuch.auth.security.CustomUserDetails;
import io.github.kaltz.feldbuch.common.exception.CustomException;
import io.github.kaltz.feldbuch.common.exception.ErrorCode;
import io.github.kaltz.feldbuch.user.entity.AuthProvider;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.reader.UserReader;
import jakarta.servlet.http.HttpServletRequest;
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
    private final RefreshTokenService refreshTokenService;
    private final UserReader userReader;

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
                        userDetails.getUser().getRole().name(),
                        AuthProvider.LOCAL.name()
                );

        String refreshToken =
                jwtProvider.createRefreshToken(
                        userDetails.getUserId()
                );

        refreshTokenService.save(
                userDetails.getUserId(),
                refreshToken
        );

        return new LoginResponse(
                userDetails.getUserId(),
                accessToken,
                refreshToken,
                "Bearer"
        );
    }

    public AuthMeResponse getCurrentUser(
            CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        String token = resolveToken(request);

        String provider = jwtProvider.getProvider(token);

        return new AuthMeResponse(
                userDetails.getUserId(),
                userDetails.getUsername(),
                userDetails.getUser().getNickname(),
                userDetails.getUser().getRole().name(),
                provider
        );
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {

            throw new CustomException(ErrorCode.TOKEN_NOT_FOUND);
        }

        return authorization.substring(7);
    }

    public RefreshTokenResponse refresh(
            RefreshTokenRequest request
    ) {
        String refreshToken =
                request.refreshToken();

        if (!jwtProvider.validateToken(refreshToken)) {
            throw new CustomException(
                    ErrorCode.INVALID_TOKEN
            );
        }

        Long userId =
                jwtProvider.getUserId(
                        refreshToken
                );

        String savedRefreshToken =
                refreshTokenService.find(
                        userId
                );

        if (
                savedRefreshToken == null
                        || !savedRefreshToken.equals(refreshToken)
        ) {
            throw new CustomException(
                    ErrorCode.INVALID_TOKEN
            );
        }

        User user =
                userReader.get(userId);

        String accessToken =
                jwtProvider.createAccessToken(
                        user.getId(),
                        user.getEmail(),
                        user.getRole().name(),
                        AuthProvider.LOCAL.name()
                );

        return new RefreshTokenResponse(
                accessToken,
                "Bearer"
        );
    }
}
