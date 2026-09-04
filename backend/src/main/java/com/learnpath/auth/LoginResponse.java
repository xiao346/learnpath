package com.learnpath.auth;

import com.learnpath.user.UserRole;

public record LoginResponse(
        String accessToken,
        long expiresInSeconds,
        UserView user
) {
    public record UserView(Long id, String account, String displayName, UserRole role) {
    }
}
