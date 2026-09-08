package com.launcher.core.architecture.support.recording;

import com.launcher.core.game.GameLaunchPlan;
import com.launcher.core.game.GameLaunchPlanBuilder;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.RuntimeLibrarySelection;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class RecordingGameLaunchPlanBuilder implements GameLaunchPlanBuilder {
    private Manifest manifest;
    private RuntimeLibrarySelection runtimeLibrarySelection;
    private Optional<String> javaExecutableOverride = Optional.empty();

    @Override
    public GameLaunchPlan build(
            Manifest manifest,
            RuntimeLibrarySelection runtimeLibrarySelection,
            Optional<String> javaExecutableOverride
    ) {
        this.manifest = manifest;
        this.runtimeLibrarySelection = runtimeLibrarySelection;
        this.javaExecutableOverride = javaExecutableOverride;

        return new GameLaunchPlan(
                Path.of("from-recording-game-launch-plan-builder"),
                List.of("execute")
        );
    }

    public Manifest getManifest() {
        return manifest;
    }

    public RuntimeLibrarySelection getRuntimeLibrarySelection() {
        return runtimeLibrarySelection;
    }

    public Optional<String> getJavaExecutableOverride() {
        return javaExecutableOverride;
    }
}
