package com.launcher.core.game;

import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.RuntimeLibrarySelection;

import java.util.Optional;

public interface GameLaunchPlanBuilder {

    GameLaunchPlan build(
            Manifest manifest,
            RuntimeLibrarySelection runtimeLibrarySelection,
            Optional<String> javaExecutableOverride
    );

}
