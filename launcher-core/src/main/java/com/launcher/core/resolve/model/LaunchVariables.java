package com.launcher.core.resolve.model;

import java.nio.file.Path;
import java.util.Objects;

public record LaunchVariables(
        String versionName,
        Path gameDirectory
) {

    public LaunchVariables {
        Objects.requireNonNull(versionName, "versionName");
        Objects.requireNonNull(gameDirectory, "gameDirectory");

        if (versionName.isBlank()) {
            throw new IllegalArgumentException("versionName must not be blank");
        }
    }

}
