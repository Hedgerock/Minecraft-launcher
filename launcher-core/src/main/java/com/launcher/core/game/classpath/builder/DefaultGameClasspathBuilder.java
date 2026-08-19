package com.launcher.core.game.classpath.builder;

import com.launcher.core.game.classpath.GameClasspath;
import com.launcher.model.manifest.LaunchInfo;

import java.nio.file.Path;
import java.util.Objects;

public final class DefaultGameClasspathBuilder implements GameClasspathBuilder {

    public GameClasspath build(LaunchInfo launchInfo, Path gameDirectory) {
        Objects.requireNonNull(launchInfo, "launchInfo");
        Objects.requireNonNull(gameDirectory, "gameDirectory");

        return new GameClasspath(
                launchInfo.classpath().stream()
                        .map(gameDirectory::resolve)
                        .toList()
        );
    }

}
