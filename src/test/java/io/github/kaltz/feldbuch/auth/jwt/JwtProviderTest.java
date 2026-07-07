package io.github.kaltz.feldbuch.auth.jwt;


import io.github.kaltz.feldbuch.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {

        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("012345678901234567890123456789012345678901234567890123456789abcd");
        jwtProperties.setAccessTokenExpiration(1000 * 60 * 30);

        jwtProvider = new JwtProvider(jwtProperties);
        jwtProvider.init();
    }

    @Test
    @DisplayName("JWT 생성 및 정보 추출 테스트")
    void createTokenTest() {

        // given
        Long userId = 1L;
        String email = "test@test.com";
        String role = "USER";

        // when
        String token = jwtProvider.createAccessToken(userId, email, role);

        // then
        System.out.println("TOKEN = " + token);

        assertThat(jwtProvider.validateToken(token)).isTrue();
        assertThat(jwtProvider.getUserId(token)).isEqualTo(userId);
        assertThat(jwtProvider.getEmail(token)).isEqualTo(email);
        assertThat(jwtProvider.getRole(token)).isEqualTo(role);
    }
}
