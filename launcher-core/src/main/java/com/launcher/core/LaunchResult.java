package com.launcher.core;

import com.launcher.core.state.LauncherState;

import java.util.Objects;

public record LaunchResult(
        boolean success,
        LauncherState finalState
) {
    public static LaunchResult success(LauncherState finalState) {
        return new LaunchResult(true, finalState);
    }

    public static LaunchResult failure(LauncherState finalState) {
        return new LaunchResult(false, finalState);
    }

    public LaunchResult {
        Objects.requireNonNull(finalState, "finalState");
    }
}
