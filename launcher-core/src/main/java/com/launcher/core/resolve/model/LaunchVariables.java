package com.launcher.core.resolve.model;

import java.nio.file.Path;
import java.util.Objects;

public record LaunchVariables(
        String versionName,
        Path gameDirectory,
        String classpath
) {

    public LaunchVariables {
        Objects.requireNonNull(versionName, "versionName");
        Objects.requireNonNull(gameDirectory, "gameDirectory");
        Objects.requireNonNull(classpath, "classpath");

        if (versionName.isBlank()) {
            throw new IllegalArgumentException("versionName must not be blank");
        }

        if (classpath.isBlank()) {
            throw new IllegalArgumentException("classpath must not be blank");
        }
    }

}
