package com.launcher.ui.startup;

public enum PresentationStartupState {
    AVAILABLE,
    CONFIGURATION_FAILED,
    IDLE;

    public boolean allowsLaunch(boolean launchRequestAvailable) {
        return this == AVAILABLE && launchRequestAvailable;
    }
}
