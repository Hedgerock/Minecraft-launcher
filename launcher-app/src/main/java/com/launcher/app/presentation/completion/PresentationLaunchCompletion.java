package com.launcher.app.presentation.completion;

import com.launcher.core.LaunchResult;

import java.util.Objects;
import java.util.Optional;

public final class PresentationLaunchCompletion {
    private final LaunchResult launchResult;

    private PresentationLaunchCompletion(LaunchResult launchResult) {
        this.launchResult = launchResult;
    }

    public static PresentationLaunchCompletion fromLaunchResult(LaunchResult launchResult) {
        Objects.requireNonNull(launchResult, "launchResult");
        return new PresentationLaunchCompletion(launchResult);
    }

    public static PresentationLaunchCompletion executionFailure() {
        return new PresentationLaunchCompletion(null);
    }

    public boolean executionFailed() {
        return launchResult == null;
    }

    public Optional<LaunchResult> launchResult() {
        return Optional.ofNullable(launchResult);
    }
}
