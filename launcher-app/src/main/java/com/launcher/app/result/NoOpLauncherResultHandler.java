package com.launcher.app.result;

import com.launcher.core.LaunchResult;

import java.util.Objects;

public final class NoOpLauncherResultHandler implements LauncherResultHandler {

    @Override
    public void handle(LaunchResult result) {
        Objects.requireNonNull(result, "result");
    }
}
