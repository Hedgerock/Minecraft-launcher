package com.launcher.ui;

import com.launcher.ui.state.PresentationLaunchState;

import java.util.Objects;

final class PresentationLaunchStatusText {

    private PresentationLaunchStatusText() {}

    static String forState(PresentationLaunchState state) {
        Objects.requireNonNull(state, "state");

        return switch (state) {
            case READY -> "Ready";
            case LAUNCHING -> "Launching...";
            case LAUNCHED -> "Game launched";
            case FAILED -> "Launch failed";
        };
    }
}
