package com.launcher.ui;

import com.launcher.app.presentation.phase.PresentationLaunchPhase;

import java.util.Objects;

final class PresentationLaunchPhaseText {

    private PresentationLaunchPhaseText() {}

    static String forPhase(PresentationLaunchPhase phase) {
        Objects.requireNonNull(phase, "phase");

        return switch (phase) {
            case LOADING_MANIFEST -> "Loading manifest...";
            case VERIFYING_FILES -> "Verifying files...";
            case DOWNLOADING -> "Downloading...";
            case PREPARING_GAME -> "Preparing game...";
            case STARTING_GAME -> "Starting game...";
        };
    }
}
