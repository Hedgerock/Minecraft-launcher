package com.launcher.core.configuration;

import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public record LauncherConfiguration(
        URI manifestUri,
        Path launcherDirectory,
        Optional<String> javaExecutableOverride
) {

    public LauncherConfiguration(URI manifestUri, Path launcherDirectory) {
        this(manifestUri, launcherDirectory, Optional.empty());
    }

    public LauncherConfiguration {
        Objects.requireNonNull(manifestUri, "manifestUri");
        Objects.requireNonNull(launcherDirectory, "launcherDirectory");
        Objects.requireNonNull(javaExecutableOverride, "javaExecutableOverride");
    }

}
