package com.launcher.app.support;

import com.launcher.app.presentation.LauncherLifecycleRunner;
import com.launcher.app.presentation.phase.PresentationLaunchPhaseHandler;
import com.launcher.core.LaunchResult;

public final class RecordingLauncherLifecycleRunner implements LauncherLifecycleRunner {
    private final LaunchResult launchResult;

    public RecordingLauncherLifecycleRunner(LaunchResult launchResult) {
        this.launchResult = launchResult;
    }

    @Override
    public LaunchResult launch(PresentationLaunchPhaseHandler presentationLaunchPhaseHandler) {
        return launchResult;
    }

    public LaunchResult getLaunchResult() {
        return launchResult;
    }
}
