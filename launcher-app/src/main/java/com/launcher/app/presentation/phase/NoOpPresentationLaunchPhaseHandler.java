package com.launcher.app.presentation.phase;

import java.util.Objects;

public final class NoOpPresentationLaunchPhaseHandler implements PresentationLaunchPhaseHandler {

    @Override
    public void handle(PresentationLaunchPhase phase) {
        Objects.requireNonNull(phase, "phase");
    }
}
