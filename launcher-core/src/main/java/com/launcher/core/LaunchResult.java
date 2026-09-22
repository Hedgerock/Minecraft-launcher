package com.launcher.core;

import com.launcher.core.state.LauncherState;

import java.util.Objects;
import java.util.Optional;

public final class LaunchResult {
    private final boolean success;
    private final LauncherState finalState;
    private final LaunchFailure failure;

    private LaunchResult(LauncherState finalState) {
        Objects.requireNonNull(finalState, "finalState");
        this.success = true;
        this.finalState = finalState;
        this.failure = null;
    }

    private LaunchResult(LauncherState finalState, LaunchFailure failure) {
        Objects.requireNonNull(finalState, "finalState");
        Objects.requireNonNull(failure, "failure");

        this.success = false;
        this.finalState = finalState;
        this.failure = failure;
    }

    public static LaunchResult success(LauncherState finalState) {
        return new LaunchResult(finalState);
    }

    public static LaunchResult failure(
            LauncherState finalState,
            LaunchFailure failure
    ) {
        return new LaunchResult(finalState, failure);
    }

    public boolean success() {
        return success;
    }

    public LauncherState finalState() {
        return finalState;
    }

    public Optional<LaunchFailure> failure() {
        return Optional.ofNullable(failure);
    }
}
