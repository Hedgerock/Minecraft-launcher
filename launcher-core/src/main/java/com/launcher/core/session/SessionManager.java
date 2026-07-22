package com.launcher.core.session;

import com.launcher.model.session.Session;

import java.util.Optional;

public interface SessionManager {

    Optional<Session> acquire();

    void open(Session session);

    void close();

}
