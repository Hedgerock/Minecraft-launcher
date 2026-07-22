package com.launcher.model.session;

import com.launcher.model.user.User;

public final class Session {

    private final User user;
    private final SessionToken token;

    public Session(User user, SessionToken token) {
        this.user = user;
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public SessionToken getToken() {
        return token;
    }
}
