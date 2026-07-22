package com.launcher.core.session;

import com.launcher.model.session.Session;

public interface SessionHandle extends AutoCloseable{
    Session session();

    @Override
    void close();

}
