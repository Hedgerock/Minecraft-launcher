package com.launcher.core.resolve.model;

import java.nio.file.Path;
import java.util.Objects;

public record LaunchVariables(
        String versionName,
        Path gameDirectory,
        String classpath,
        Path nativesDirectory,
        Path assetsRoot,
        String assetsIndexName
) {

    public LaunchVariables(
            String versionName,
            Path gameDirectory,
            String classpath,
            Path nativesDirectory,
            Path assetsRoot
    ) {
        this(
                versionName,
                gameDirectory,
                classpath,
                nativesDirectory,
                assetsRoot,
                null
        );
    }

    public LaunchVariables {
        Objects.requireNonNull(versionName, "versionName");
        Objects.requireNonNull(gameDirectory, "gameDirectory");
        Objects.requireNonNull(classpath, "classpath");
        Objects.requireNonNull(nativesDirectory, "nativesDirectory");
        Objects.requireNonNull(assetsRoot, "assetsRoot");

        validateNotBlank(versionName, "versionName");
        validateNotBlank(classpath, "classpath");

        if (assetsIndexName != null) {
            validateNotBlank(assetsIndexName, "assetsIndexName");
        }
    }

    private void validateNotBlank(String value, String fieldName) {
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " " + "must not be blank");
        }
    }

}
