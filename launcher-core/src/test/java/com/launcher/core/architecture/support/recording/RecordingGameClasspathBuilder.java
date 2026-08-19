package com.launcher.core.architecture.support.recording;

import com.launcher.core.game.classpath.GameClasspath;
import com.launcher.core.game.classpath.builder.GameClasspathBuilder;
import com.launcher.model.manifest.LaunchInfo;

import java.nio.file.Path;
import java.util.List;

public final class RecordingGameClasspathBuilder implements GameClasspathBuilder {

    private LaunchInfo launchInfo;
    private Path gameDirectory;
    private GameClasspath gameClasspath;

    @Override
    public GameClasspath build(LaunchInfo launchInfo, Path gameDirectory) {
        this.gameDirectory = gameDirectory;
        this.launchInfo = launchInfo;

        this.gameClasspath = new GameClasspath(
                List.of(Path.of("current-path"))
        );

        return gameClasspath;
    }

    public LaunchInfo getLaunchInfo() {
        return launchInfo;
    }

    public Path getGameDirectory() {
        return gameDirectory;
    }

    public GameClasspath getGameClasspath() {
        return gameClasspath;
    }
}
