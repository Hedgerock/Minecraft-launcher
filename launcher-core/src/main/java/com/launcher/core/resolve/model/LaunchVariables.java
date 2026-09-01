package com.launcher.core.resolve.model;

import java.nio.file.Path;
import java.util.Objects;

public record LaunchVariables(
        String versionName,
        Path gameDirectory,
        String classpath,
        Path nativesDirectory
) {

    public LaunchVariables {
        Objects.requireNonNull(versionName, "versionName");
        Objects.requireNonNull(gameDirectory, "gameDirectory");
        Objects.requireNonNull(classpath, "classpath");
        Objects.requireNonNull(nativesDirectory, "nativesDirectory");

        validateNotBlank(versionName, "versionName");
        validateNotBlank(classpath, "classpath");
    }

    private void validateNotBlank(String value, String fieldName) {
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " " + "must not be blank");
        }
    }

}
