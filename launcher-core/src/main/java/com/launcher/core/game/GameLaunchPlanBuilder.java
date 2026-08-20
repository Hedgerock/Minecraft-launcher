package com.launcher.core.game;

import com.launcher.core.game.builder.GameLaunchCommandBuilder;
import com.launcher.core.game.classpath.GameClasspath;
import com.launcher.core.game.classpath.builder.GameClasspathBuilder;
import com.launcher.core.game.classpath.formatter.ClasspathFormatter;
import com.launcher.core.resolve.model.LaunchVariables;
import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.model.manifest.Manifest;

import java.nio.file.Path;

public final class GameLaunchPlanBuilder {
    private final DirectoryProvider directoryProvider;
    private final GameLaunchCommandBuilder launchCommandBuilder;
    private final GameClasspathBuilder gameClasspathBuilder;
    private final ClasspathFormatter classpathFormatter;

    public GameLaunchPlanBuilder(
            DirectoryProvider directoryProvider,
            GameLaunchCommandBuilder launchCommandBuilder,
            GameClasspathBuilder gameClasspathBuilder,
            ClasspathFormatter classpathFormatter
    ) {
        this.directoryProvider = directoryProvider;
        this.launchCommandBuilder = launchCommandBuilder;
        this.gameClasspathBuilder = gameClasspathBuilder;
        this.classpathFormatter = classpathFormatter;
    }

    public GameLaunchPlan build(Manifest manifest) {
        Path gameDirectory = directoryProvider.directories().game();

        GameClasspath gameClasspath = gameClasspathBuilder.build(
                manifest,
                gameDirectory
        );

        String classpath = classpathFormatter.format(gameClasspath);

        LaunchVariables launchVariables = new LaunchVariables(
                manifest.minecraftVersion(),
                gameDirectory,
                classpath
        );

        return new GameLaunchPlan(
                gameDirectory,
                launchCommandBuilder.build(manifest.launchInfo(), launchVariables)
        );
    }

}
