package com.launcher.core.game;

import com.launcher.core.game.builder.GameLaunchCommandBuilder;
import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.model.manifest.Manifest;

public final class GameLaunchPlanBuilder {
    private final DirectoryProvider directoryProvider;
    private final GameLaunchCommandBuilder launchCommandBuilder;

    public GameLaunchPlanBuilder(DirectoryProvider directoryProvider, GameLaunchCommandBuilder launchCommandBuilder) {
        this.directoryProvider = directoryProvider;
        this.launchCommandBuilder = launchCommandBuilder;
    }

    public GameLaunchPlan build(Manifest manifest) {
        return new GameLaunchPlan(
                directoryProvider.directories().game(),
                launchCommandBuilder.build(manifest.launchInfo())
        );
    }

}
