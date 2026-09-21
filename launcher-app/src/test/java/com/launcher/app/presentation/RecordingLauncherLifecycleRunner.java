package com.launcher.app.presentation;

import com.launcher.core.LaunchResult;

final class RecordingLauncherLifecycleRunner implements LauncherLifecycleRunner {
    private final LaunchResult launchResult;

    RecordingLauncherLifecycleRunner(LaunchResult launchResult) {
        this.launchResult = launchResult;
    }

    @Override
    public LaunchResult launch() {
        return launchResult;
    }

    LaunchResult getLaunchResult() {
        return launchResult;
    }
}
