package io.github.kaltz.feldbuch.auth.oauth2;

import io.github.kaltz.feldbuch.auth.jwt.JwtProvider;
import io.github.kaltz.feldbuch.user.entity.AuthProvider;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.reader.UserReader;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private final JwtProvider jwtProvider;

    private final UserReader userReader;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        FeldbuchOidcUser oidcUser =
                extractOidcUser(authentication);

        User user =
                userReader.get(oidcUser.getUserId());

        String accessToken =
                jwtProvider.createAccessToken(
                        user.getId(),
                        user.getEmail(),
                        user.getRole().name(),
                        AuthProvider.GOOGLE.name()
                );

        String redirectUrl =
                UriComponentsBuilder
                        .fromUriString(
                                frontendUrl + "/oauth2/success"
                        )
                        .queryParam(
                                "token",
                                accessToken
                        )
                        .queryParam(
                                "userId",
                                user.getId()
                        )
                        .build()
                        .encode()
                        .toUriString();

        log.info("[OAUTH2_LOGIN_SUCCESS] frontendUrl={}", frontendUrl);

        response.sendRedirect(redirectUrl);
    }

    private FeldbuchOidcUser extractOidcUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof FeldbuchOidcUser)) {
            throw new IllegalStateException(
                    "Google 로그인 사용자 정보를 확인할 수 없습니다."
            );
        }

        return (FeldbuchOidcUser) authentication.getPrincipal();
    }
}
