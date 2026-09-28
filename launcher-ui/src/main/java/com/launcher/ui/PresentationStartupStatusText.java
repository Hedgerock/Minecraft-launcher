package com.launcher.ui;

import com.launcher.ui.startup.PresentationStartupState;

import java.util.Objects;

final class PresentationStartupStatusText {

    static String forState(PresentationStartupState state) {
        Objects.requireNonNull(state, "state");

        return switch (state) {
            case AVAILABLE, IDLE -> "";
            case CONFIGURATION_FAILED -> "Could not load launch configuration";
        };
    }
}
