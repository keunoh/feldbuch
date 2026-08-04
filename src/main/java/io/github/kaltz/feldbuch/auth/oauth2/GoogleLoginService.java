package io.github.kaltz.feldbuch.auth.oauth2;

import io.github.kaltz.feldbuch.common.exception.CustomException;
import io.github.kaltz.feldbuch.common.exception.ErrorCode;
import io.github.kaltz.feldbuch.user.entity.AuthProvider;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.entity.UserIdentity;
import io.github.kaltz.feldbuch.user.repository.UserIdentityRepository;
import io.github.kaltz.feldbuch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GoogleLoginService {

    private final UserRepository userRepository;
    private final UserIdentityRepository userIdentityRepository;
    private final PasswordEncoder passwordEncoder;

    public User login(
            String subject,
            String email,
            String name,
            boolean emailVerified
    ) {
        validateGoogleAccount(
                subject,
                email,
                emailVerified
        );

        return userIdentityRepository
                .findByProviderAndProviderSubject(
                        AuthProvider.GOOGLE,
                        subject
                )
                .map(UserIdentity::getUser)
                .orElseGet(() ->
                        createOrLinkIdentity(
                                subject,
                                email,
                                name
                        )
                );
    }

    private User createOrLinkIdentity(
            String subject,
            String email,
            String name
    ) {
        String normalizedEmail =
                normalizeEmail(email);

        User user =
                userRepository
                        .findByEmail(normalizedEmail)
                        .orElseGet(() ->
                                createGoogleUser(
                                        normalizedEmail,
                                        name
                                )
                        );

        UserIdentity identity =
                UserIdentity.createGoogle(
                        user,
                        subject,
                        normalizedEmail
                );

        userIdentityRepository.save(identity);

        return user;
    }

    private User createGoogleUser(
            String email,
            String name
    ) {
        String randomPassword =
                UUID.randomUUID().toString();

        User user =
                User.builder()
                        .email(email)
                        .password(
                                passwordEncoder.encode(
                                        randomPassword
                                )
                        )
                        .nickname(
                                createNickname(
                                        name,
                                        email
                                )
                        )
                        .build();

        return userRepository.save(user);
    }

    private void validateGoogleAccount(
            String subject,
            String email,
            boolean emailVerified
    ) {
        if (subject == null || subject.isBlank()) {
            throw new CustomException(
                    ErrorCode.INVALID_GOOGLE_ACCOUNT
            );
        }

        if (email == null || email.isBlank()) {
            throw new CustomException(
                    ErrorCode.INVALID_GOOGLE_ACCOUNT
            );
        }

        if (!emailVerified) {
            throw new CustomException(
                    ErrorCode.INVALID_GOOGLE_ACCOUNT
            );
        }
    }

    private String normalizeEmail(String email) {
        return email
                .trim()
                .toLowerCase();
    }

    private String createNickname(
            String name,
            String email
    ) {
        String candidate;

        if (name == null || name.isBlank()) {
            candidate = extractEmailPrefix(email);
        } else {
            candidate = name.trim();
        }

        if (candidate.length() <= 30) {
            return candidate;
        }

        return candidate.substring(0, 30);
    }

    private String extractEmailPrefix(String email) {
        int separatorIndex =
                email.indexOf('@');

        if (separatorIndex <= 0) {
            return "Google 사용자";
        }

        return email.substring(0, separatorIndex);
    }
}
