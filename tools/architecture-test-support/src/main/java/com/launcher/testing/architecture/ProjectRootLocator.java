package com.launcher.testing.architecture;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class ProjectRootLocator {

    public Path findFrom(Path startDirectory) {
        Objects.requireNonNull(startDirectory, "startDirectory");

        Path currentPath = startDirectory.toAbsolutePath();

        if (!Files.isDirectory(currentPath)) {
            throw new IllegalArgumentException(
                    "startDirectory must be an existing directory"
            );
        }

        while (currentPath != null) {
            boolean isProjectRoot = Files.isRegularFile(currentPath.resolve("settings.gradle")) ||
                    Files.isRegularFile(currentPath.resolve("settings.gradle.kts"));

            if (isProjectRoot) {
                return currentPath;
            }

            currentPath = currentPath.getParent();
        }

        throw new IllegalStateException("Project root not found");
    }
}
