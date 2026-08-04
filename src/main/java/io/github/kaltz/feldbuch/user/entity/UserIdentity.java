package io.github.kaltz.feldbuch.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "user_identities",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_identity_provider_subject",
                        columnNames = {
                                "provider",
                                "provider_subject"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_user_identity_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_user_identity_provider_email",
                        columnList = "provider, provider_email"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserIdentity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_user_identity_user"
            )
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private AuthProvider provider;

    /**
     * OAuth Provider가 발급한 사용자의 고유 식별자
     * <p>
     * Google OIDC에서는 sub claim을 저장한다.
     */
    @Column(
            name = "provider_subject",
            nullable = false,
            length = 255
    )
    private String providerSubject;

    @Column(
            name = "provider_email",
            length = 100
    )
    private String providerEmail;

    private UserIdentity(
            User user,
            AuthProvider provider,
            String providerSubject,
            String providerEmail
    ) {
        validateUser(user);
        validateProvider(provider);
        validateProviderSubject(providerSubject);

        this.user = user;
        this.provider = provider;
        this.providerSubject = providerSubject.trim();
        this.providerEmail = normalizeEmail(providerEmail);
    }

    public static UserIdentity createGoogle(
            User user,
            String subject,
            String email
    ) {
        return new UserIdentity(
                user,
                AuthProvider.GOOGLE,
                subject,
                email
        );
    }

    public boolean belongsTo(User user) {
        return this.user == user;
    }

    private static void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "사용자는 필수입니다."
            );
        }
    }

    private static void validateProvider(AuthProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException(
                    "인증 제공자는 필수입니다."
            );
        }
    }

    private static void validateProviderSubject(String providerSubject) {
        if (providerSubject == null || providerSubject.isBlank()) {
            throw new IllegalArgumentException(
                    "인증 제공자 사용자 식별자는 필수입니다."
            );
        }

        if (providerSubject.trim().length() > 255) {
            throw new IllegalArgumentException(
                    "인증 제공자 사용자 식별자는 255자를 초과할 수 없습니다."
            );
        }
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        String normalized = email.trim().toLowerCase();

        if (normalized.length() > 100) {
            throw new IllegalArgumentException(
                    "인증 제공자 이메일은 100자를 초과할 수 없습니다."
            );
        }

        return normalized;
    }
}
