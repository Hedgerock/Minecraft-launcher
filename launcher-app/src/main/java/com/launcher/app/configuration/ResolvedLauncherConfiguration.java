package com.launcher.app.configuration;

import com.launcher.core.configuration.LauncherConfiguration;

import java.util.Objects;

public record ResolvedLauncherConfiguration(
        LauncherConfiguration configuration,
        ManifestSourceKind sourceKind
) {

    public ResolvedLauncherConfiguration {
        Objects.requireNonNull(configuration, "configuration");
        Objects.requireNonNull(sourceKind, "sourceKind");
    }
}
