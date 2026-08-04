package io.github.kaltz.feldbuch.auth.oauth2;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Map;

public final class FeldbuchOidcUser implements OidcUser {

    private final OidcUser delegate;
    private final Long userId;

    public FeldbuchOidcUser(
            OidcUser delegate,
            Long userId
    ) {
        if (delegate == null) {
            throw new IllegalArgumentException(
                    "OIDC 사용자는 필수입니다."
            );
        }

        if (userId == null) {
            throw new IllegalArgumentException(
                    "Feldbuch 사용자 ID는 필수입니다."
            );
        }

        this.delegate = delegate;
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public Map<String, Object> getClaims() {
        return delegate.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return delegate.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return delegate.getIdToken();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return delegate.getAuthorities();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }
}
