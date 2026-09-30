package com.launcher.ui.startup;

import java.util.Objects;

public record PresentationStartupResult(
        PresentationStartupState state,
        boolean retryAvailable
) {

    public PresentationStartupResult {
        Objects.requireNonNull(state, "state");

        if (retryAvailable && state != PresentationStartupState.CONFIGURATION_FAILED) {
            throw new IllegalArgumentException(
                    "Retry requires configuration failure"
            );
        }
    }

    public static PresentationStartupResult available() {
        return new PresentationStartupResult(PresentationStartupState.AVAILABLE, false);
    }

    public static PresentationStartupResult localConfigurationFailed() {
        return new PresentationStartupResult(PresentationStartupState.CONFIGURATION_FAILED, true);
    }

    public static PresentationStartupResult configurationFailed() {
        return new PresentationStartupResult(PresentationStartupState.CONFIGURATION_FAILED, false);
    }
}
