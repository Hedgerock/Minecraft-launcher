package com.launcher.app.configuration.path;

import java.nio.file.Path;
import java.util.Objects;

public record LauncherUserPaths(
        Path configurationFile,
        Path defaultLauncherDirectory
) {

    public LauncherUserPaths {
        Objects.requireNonNull(configurationFile, "configurationFile");
        Objects.requireNonNull(defaultLauncherDirectory, "defaultLauncherDirectory");
    }
}
