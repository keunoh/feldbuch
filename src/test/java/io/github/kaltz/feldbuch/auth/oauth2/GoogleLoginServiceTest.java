package io.github.kaltz.feldbuch.auth.oauth2;

import io.github.kaltz.feldbuch.common.exception.CustomException;
import io.github.kaltz.feldbuch.common.exception.ErrorCode;
import io.github.kaltz.feldbuch.user.entity.AuthProvider;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.entity.UserIdentity;
import io.github.kaltz.feldbuch.user.repository.UserIdentityRepository;
import io.github.kaltz.feldbuch.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleLoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserIdentityRepository userIdentityRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private GoogleLoginService googleLoginService;

    @Mock
    private User user;

    @Mock
    private UserIdentity identity;

    @Test
    void 이미_연결된_Google_계정이면_기존_사용자를_반환한다() {
        // given
        String subject = "google-subject";
        String email = "user@gmail.com";

        when(
                userIdentityRepository
                        .findByProviderAndProviderSubject(
                                AuthProvider.GOOGLE,
                                subject
                        )
        ).thenReturn(Optional.of(identity));

        when(identity.getUser())
                .thenReturn(user);

        // when
        User result =
                googleLoginService.login(
                        subject,
                        email,
                        "근오",
                        true
                );

        // then
        assertThat(result)
                .isSameAs(user);

        verify(userRepository, never())
                .findByEmail(any());

        verify(userIdentityRepository, never())
                .save(any());
    }

    @Test
    void 같은_이메일의_사용자가_있으면_Google_계정을_연결한다() {
        // given
        String subject = "google-subject";
        String email = "USER@GMAIL.COM";

        when(
                userIdentityRepository
                        .findByProviderAndProviderSubject(
                                AuthProvider.GOOGLE,
                                subject
                        )
        ).thenReturn(Optional.of(identity));

        when(
                userRepository.findByEmail(
                        "user@gmail.com"
                )
        ).thenReturn(Optional.of(user));

        // when
        User result =
                googleLoginService.login(
                        subject,
                        email,
                        "근오",
                        true
                );

        // then
        assertThat(result)
                .isSameAs(user);

        verify(userIdentityRepository)
                .save(
                        any(UserIdentity.class)
                );

        verify(userRepository, never())
                .save(any());
    }

    @Test
    void 사용자가_없으면_Google_사용자를_신규_생성한다() {
        // given
        String subject = "google-subject";
        String email = "user@gmail.com";

        when(
                userIdentityRepository
                        .findByProviderAndProviderSubject(
                                AuthProvider.GOOGLE,
                                subject
                        )
        ).thenReturn(Optional.of(identity));

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(any()))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        // when
        User result =
                googleLoginService.login(
                        subject,
                        email,
                        "근오",
                        true
                );

        // then
        assertThat(result.getEmail())
                .isEqualTo(email);

        assertThat(result.getNickname())
                .isEqualTo("근오");

        assertThat(result.getPassword())
                .isEqualTo("encoded-password");

        verify(userRepository)
                .save(any(User.class));

        verify(userIdentityRepository)
                .save(any(UserIdentity.class));
    }

    @Test
    void 인증되지_않은_Google_이메일이면_실패한다() {
        assertThatThrownBy(() ->
                googleLoginService.login(
                        "google-subject",
                        "user@gmail.com",
                        "근오",
                        false
                )
        )
                .isInstanceOf(
                        CustomException.class
                );

        verify(userIdentityRepository, never())
                .findByProviderAndProviderSubject(
                        any(),
                        any()
                );
    }

    @Test
    void 인증되지_않은_Google_이메일이면_실패한다2() {
        CustomException exception =
                catchThrowableOfType(
                        () ->
                                googleLoginService.login(
                                        "google-subject",
                                        "user@gmail.com",
                                        "근오",
                                        false
                                ),
                        CustomException.class
                );

        assertThat(exception.getErrorCode())
                .isEqualTo(
                        ErrorCode.INVALID_GOOGLE_ACCOUNT
                );

        verify(userIdentityRepository, never())
                .findByProviderAndProviderSubject(
                        any(),
                        any()
                );
    }

    @Test
    void Google_사용자_식별자가_없으면_실패한다() {
        assertThatThrownBy(() ->
                googleLoginService.login(
                        " ",
                        "user@gmail.com",
                        "근오",
                        true
                )
        )
                .isInstanceOf(
                        CustomException.class
                );

        verifyNoInteractions(
                userRepository,
                userIdentityRepository,
                passwordEncoder
        );
    }

    @Test
    void Google_이메일이_없으면_실패한다() {
        assertThatThrownBy(() ->
                googleLoginService.login(
                        "google-subject",
                        null,
                        "근오",
                        true
                )
        )
                .isInstanceOf(
                        CustomException.class
                );

        verifyNoInteractions(
                userRepository,
                userIdentityRepository,
                passwordEncoder
        );
    }
}