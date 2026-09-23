package com.launcher.ui.failure;

import com.launcher.core.LaunchFailure;
import com.launcher.core.operation.type.OperationType;

import java.util.Objects;

public final class PresentationLaunchFailureMapper {

    public PresentationLaunchFailure map(LaunchFailure failure) {
        Objects.requireNonNull(failure, "failure");

        if (failure.operationType().isEmpty()) {
            return presentationLaunchFailure("Launch failed");
        }

        OperationType operationType = failure.operationType().orElseThrow();

        return relativeMessage(operationType);
    }

    private PresentationLaunchFailure relativeMessage(OperationType operationType) {
        return switch (operationType) {
            case LOAD_MANIFEST
                    -> presentationLaunchFailure("Could not load game information");
            case VERIFY_FILES
                    -> presentationLaunchFailure("Could not verify game files");
            case DOWNLOAD_FILES
                    -> presentationLaunchFailure("Could not download game files");
            case REPAIR, BUILD_DOWNLOAD_PLAN, PREPARE_DIRECTORIES, EXTRACT_NATIVES
                    -> presentationLaunchFailure("Could not prepare game files");
            case BUILD_GAME_LAUNCH_PLAN, LAUNCH_GAME
                    -> presentationLaunchFailure("Could not start the game");
        };
    }

    private PresentationLaunchFailure presentationLaunchFailure(String message) {
        return new PresentationLaunchFailure(message);
    }
}
