package com.launcher.app.presentation;

public interface PresentationLaunchBoundary extends AutoCloseable {

    LaunchRequestResult requestLaunch();

    @Override
    void close();
}
