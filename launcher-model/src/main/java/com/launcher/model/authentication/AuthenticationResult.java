package com.launcher.model.authentication;

import com.launcher.model.session.Session;

public class AuthenticationResult {
    private final Session session;

    public AuthenticationResult(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }
}
