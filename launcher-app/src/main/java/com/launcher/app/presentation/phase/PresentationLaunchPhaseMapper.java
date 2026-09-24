package com.launcher.app.presentation.phase;

import com.launcher.core.state.LauncherState;

import java.util.Objects;
import java.util.Optional;

public final class PresentationLaunchPhaseMapper {

    public Optional<PresentationLaunchPhase> map(LauncherState state) {
        Objects.requireNonNull(state, "state");

        return switch (state) {
            case LauncherState.LOADING_MANIFEST
                    -> Optional.of(PresentationLaunchPhase.LOADING_MANIFEST);

            case LauncherState.VERIFYING_FILES
                    -> Optional.of(PresentationLaunchPhase.VERIFYING_FILES);

            case LauncherState.DOWNLOADING
                    -> Optional.of(PresentationLaunchPhase.DOWNLOADING);

            case LauncherState.PREPARING_GAME,
                 LauncherState.EXTRACTING_NATIVES,
                 LauncherState.BUILDING_GAME_LAUNCH_PLAN
                    -> Optional.of(PresentationLaunchPhase.PREPARING_GAME);

            case LauncherState.LAUNCHING
                    -> Optional.of(PresentationLaunchPhase.STARTING_GAME);

            case LauncherState.IDLE,
                 LauncherState.CHECKING_UPDATES,
                 LauncherState.BUILDING_DOWNLOAD_PLAN,
                 LauncherState.RUNNING,
                 LauncherState.FAILED
                    -> Optional.empty();
        };
    }

}
