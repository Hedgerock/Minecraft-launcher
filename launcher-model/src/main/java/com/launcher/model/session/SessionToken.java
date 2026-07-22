package com.launcher.model.session;

import java.time.Instant;

public final class SessionToken {
    private final String accessToken;
    private final Instant expiresAt;

    public SessionToken(String accessToken, Instant expiresAt) {
        this.accessToken = accessToken;
        this.expiresAt = expiresAt;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
