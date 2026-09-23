package com.launcher.ui;

import com.launcher.ui.failure.PresentationLaunchFailure;
import com.launcher.ui.state.PresentationLaunchState;

import java.util.Objects;
import java.util.Optional;

final class PresentationLaunchStatusText {

    private PresentationLaunchStatusText() {}

    static String forState(
            PresentationLaunchState state,
            Optional<PresentationLaunchFailure> failure
    ) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(failure, "failure");

        return switch (state) {
            case READY -> "Ready";
            case LAUNCHING -> "Launching...";
            case LAUNCHED -> "Game launched";
            case FAILED -> failure
                    .orElseThrow(() -> new IllegalStateException("Failed requires presentation failure"))
                    .message();
        };
    }
}
