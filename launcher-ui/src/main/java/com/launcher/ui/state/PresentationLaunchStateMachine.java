package com.launcher.ui.state;

import com.launcher.app.presentation.LaunchRequestResult;
import com.launcher.app.presentation.completion.PresentationLaunchCompletion;
import com.launcher.core.LaunchResult;
import com.launcher.ui.failure.PresentationLaunchFailure;
import com.launcher.ui.failure.PresentationLaunchFailureMapper;

import java.util.Objects;
import java.util.Optional;

public final class PresentationLaunchStateMachine {
    private PresentationLaunchState currentState = PresentationLaunchState.READY;
    private PresentationLaunchFailure launchFailure;
    private final PresentationLaunchFailureMapper mapper = new PresentationLaunchFailureMapper();

    public PresentationLaunchState currentState() {
        return currentState;
    }

    public boolean isLaunchAvailable() {
        return currentState != PresentationLaunchState.LAUNCHING;
    }

    public Optional<PresentationLaunchFailure> launchFailure() {
        return Optional.ofNullable(launchFailure);
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

                launchFailure = null;
                this.currentState = PresentationLaunchState.LAUNCHING;
            }
            case REJECTED_ALREADY_RUNNING -> {}
        }
    }

    public void onLaunchCompletion(PresentationLaunchCompletion completion) {
        Objects.requireNonNull(completion, "completion");

        if (this.currentState != PresentationLaunchState.LAUNCHING) {
            throw new IllegalStateException(
                    "Unexpected state for completion"
            );
        }

        if (completion.executionFailed()) {
            this.currentState = PresentationLaunchState.FAILED;

            launchFailure = mapper.mapExecutionFailure();
            return;
        }

        LaunchResult launchResult = completion.launchResult().orElseThrow();

        if (launchResult.success()) {
            this.currentState = PresentationLaunchState.LAUNCHED;

            launchFailure = null;
            return;
        }

        launchFailure = mapper.map(launchResult.failure().orElseThrow());

        this.currentState = PresentationLaunchState.FAILED;
    }
}
