package com.launcher.app.support;

import com.launcher.app.presentation.phase.PresentationLaunchPhase;
import com.launcher.app.presentation.phase.PresentationLaunchPhaseHandler;

public class RecordingPresentationLaunchPhaseHandler implements PresentationLaunchPhaseHandler {
    private PresentationLaunchPhase phase;

    @Override
    public void handle(PresentationLaunchPhase phase) {
        this.phase = phase;
    }

    public PresentationLaunchPhase getPhase() {
        return phase;
    }
}
