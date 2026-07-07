package io.github.kaltz.feldbuch.runner;

import io.github.kaltz.feldbuch.auth.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTestRunner implements CommandLineRunner {

    private final JwtProvider jwtProvider;

    @Override
    public void run(String... args) throws Exception {

        String token = jwtProvider.createAccessToken(
                1L,
                "test@test.com",
                "USER"
        );

        System.out.println("==============================");
        System.out.println(token);
        System.out.println(jwtProvider.getUserId(token));
        System.out.println(jwtProvider.getEmail(token));
        System.out.println(jwtProvider.getRole(token));
        System.out.println("==============================");
    }
}
