package com.launcher.core.session;

import com.launcher.model.session.Session;

import java.util.Optional;

public final class DefaultSessionManager implements SessionManager {

    private Session session;

    @Override
    public Optional<Session> acquire() {
        return Optional.empty();
    }

    @Override
    public void open(Session session) {
        this.session = session;
    }

    @Override
    public void close() {
        this.session = null;
    }
}
