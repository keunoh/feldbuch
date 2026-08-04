package io.github.kaltz.feldbuch.user.repository;

import io.github.kaltz.feldbuch.user.entity.AuthProvider;
import io.github.kaltz.feldbuch.user.entity.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserIdentityRepository extends JpaRepository<UserIdentity, Long> {

    Optional<UserIdentity> findByProviderAndProviderSubject(AuthProvider provider, String providerSubject);

    boolean existsByProviderAndProviderSubject(AuthProvider provider, String providerSubject);
}
