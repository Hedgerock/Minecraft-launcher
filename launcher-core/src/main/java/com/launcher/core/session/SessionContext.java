package com.launcher.core.session;

import com.launcher.model.session.Session;

public final class SessionContext {
    private Session session;

    public SessionContext(Session session) {
        this.session = session;
    }

    public SessionContext setSession(Session session) {
        this.session = session;
        return this;
    }
}
