package io.github.kaltz.feldbuch.auth.oauth2;

import io.github.kaltz.feldbuch.common.exception.CustomException;
import io.github.kaltz.feldbuch.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleOidcUserService extends OidcUserService {

    private static final String GOOGLE_LOGIN_FAILED =
            "google_login_failed";

    private final GoogleLoginService googleLoginService;

    @Override
    public OidcUser loadUser(
            OidcUserRequest userRequest
    ) throws OAuth2AuthenticationException {

        OidcUser oidcUser = super.loadUser(userRequest);

        try {
            User user =
                    googleLoginService.login(
                            oidcUser.getSubject(),
                            oidcUser.getEmail(),
                            oidcUser.getFullName(),
                            Boolean.TRUE.equals(
                                    oidcUser.getEmailVerified()
                            )
                    );

            return new FeldbuchOidcUser(
                    oidcUser,
                    user.getId()
            );
        } catch (CustomException exception) {
            throw convertAuthenticationException(
                    exception
            );
        }
    }

    private OAuth2AuthenticationException convertAuthenticationException(
            CustomException exception
    ) {
        OAuth2Error error =
                new OAuth2Error(
                        GOOGLE_LOGIN_FAILED,
                        exception.getMessage(),
                        null
                );

        return new OAuth2AuthenticationException(
                error,
                exception.getMessage(),
                exception
        );
    }
}
