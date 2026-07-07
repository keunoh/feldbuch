package io.github.kaltz.feldbuch.auth.jwt;

import io.github.kaltz.feldbuch.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    public String createAccessToken(Long userId, String email, String role) {

        Date now = new Date();

        Date expiration = new Date(
                now.getTime() + jwtProperties.getAccessTokenExpiration()
        );

        // 왜 subject에 userId를 넣는가?
        // 이메일은 변경될 수 있다.
        // 하지만 ID는 절대 변하지 않는다.
        // 그래서 JWT의 주체(subject)에는 PK를 넣는 것이 일반적이다.

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role)
                .issuedAt(expiration)
                .signWith(secretKey)
                .compact();
    }

    public Claims parseClaims(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {

        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public String getEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }
}
