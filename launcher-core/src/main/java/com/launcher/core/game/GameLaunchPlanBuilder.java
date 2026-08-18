package com.launcher.core.game;

import com.launcher.core.game.builder.GameLaunchCommandBuilder;
import com.launcher.core.resolve.model.LaunchVariables;
import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.model.manifest.Manifest;

import java.nio.file.Path;

public final class GameLaunchPlanBuilder {
    private final DirectoryProvider directoryProvider;
    private final GameLaunchCommandBuilder launchCommandBuilder;

    public GameLaunchPlanBuilder(DirectoryProvider directoryProvider, GameLaunchCommandBuilder launchCommandBuilder) {
        this.directoryProvider = directoryProvider;
        this.launchCommandBuilder = launchCommandBuilder;
    }

    public GameLaunchPlan build(Manifest manifest) {
        Path gameDirectory = directoryProvider.directories().game();

        LaunchVariables launchVariables = new LaunchVariables(
                manifest.minecraftVersion(),
                gameDirectory
        );

        return new GameLaunchPlan(
                gameDirectory,
                launchCommandBuilder.build(manifest.launchInfo(), launchVariables)
        );
    }

}
