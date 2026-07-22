package com.launcher.model.authentication;

public final class AuthenticationRequest {
    private final Credentials credentials;
    private final AuthenticationProviderType providerType;

    public AuthenticationRequest(Credentials credentials, AuthenticationProviderType providerType) {
        this.credentials = credentials;
        this.providerType = providerType;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public AuthenticationProviderType getProviderType() {
        return providerType;
    }
}
