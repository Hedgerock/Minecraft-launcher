package com.launcher.ui.state;

import com.launcher.app.presentation.LaunchRequestResult;
import com.launcher.core.LaunchResult;

import java.util.Objects;

public final class PresentationLaunchStateMachine {
    private PresentationLaunchState currentState = PresentationLaunchState.READY;

    public PresentationLaunchState currentState() {
        return currentState;
    }

    public boolean isLaunchAvailable() {
        return currentState != PresentationLaunchState.LAUNCHING;
    }

    public void onLaunchRequest(LaunchRequestResult result) {
        Objects.requireNonNull(result, "result");

        switch (result) {
            case ACCEPTED -> {
                if (this.currentState == PresentationLaunchState.LAUNCHING) {
                    throw new IllegalStateException(
                            "Launch request cannot be accepted while launch is running"
                    );
                }

                this.currentState = PresentationLaunchState.LAUNCHING;
            }
            case REJECTED_ALREADY_RUNNING -> {}
        }
    }

    public void onLaunchResult(LaunchResult result) {
        Objects.requireNonNull(result, "result");

        if (this.currentState != PresentationLaunchState.LAUNCHING) {
            throw new IllegalStateException(
                    "Unexpected state for launch result"
            );
        }

        if (result.success()) {
            this.currentState = PresentationLaunchState.LAUNCHED;
            return;
        }

        this.currentState = PresentationLaunchState.FAILED;
    }
}
