package io.github.kaltz.feldbuch.support;

import io.github.kaltz.feldbuch.user.entity.User;

public record TestAuthentication(
        User user,
        String accessToken
) {
}
