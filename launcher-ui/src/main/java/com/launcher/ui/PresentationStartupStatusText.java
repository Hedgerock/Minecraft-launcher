package com.launcher.ui;

import com.launcher.ui.startup.PresentationStartupResult;

import java.util.Objects;

final class PresentationStartupStatusText {

    static String forResult(PresentationStartupResult startupResult) {
        Objects.requireNonNull(startupResult, "startupResult");

        if (startupResult.retryAvailable()) {
            return "Check keystone.properties, then press Retry";
        }

        return switch (startupResult.state()) {
            case AVAILABLE, IDLE -> "";
            case CONFIGURATION_FAILED -> "Could not load launch configuration";
        };
    }
}
