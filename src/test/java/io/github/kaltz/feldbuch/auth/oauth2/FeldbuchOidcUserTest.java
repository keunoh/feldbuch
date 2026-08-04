package io.github.kaltz.feldbuch.auth.oauth2;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FeldbuchOidcUserTest {

    @Test
    void 기존_OidcUser에_Feldbuch_사용자_ID를_추가한다() {
        // given
        OidcIdToken idToken =
                new OidcIdToken(
                        "id-token",
                        Instant.now(),
                        Instant.now().plusSeconds(300),
                        Map.of(
                                "sub", "google-subject",
                                "email", "user@gmail.com",
                                "name", "근오"
                        )
                );

        OidcUser oidcUser =
                new DefaultOidcUser(
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_USER"
                                )
                        ),
                        idToken
                );

        // when
        FeldbuchOidcUser result =
                new FeldbuchOidcUser(
                        oidcUser,
                        1L
                );

        // then
        assertThat(result.getUserId())
                .isEqualTo(1L);

        assertThat(result.getSubject())
                .isEqualTo("google-subject");

        assertThat(result.getEmail())
                .isEqualTo("user@gmail.com");

        assertThat(result.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");

    }

}