package com.launcher.app.configuration;

import com.launcher.core.configuration.LauncherConfiguration;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

public record ResolvedLauncherConfiguration(
        LauncherConfiguration configuration,
        ManifestSourceKind sourceKind,
        Optional<ManagedManifestUris> managedManifestUris
) {
    public ResolvedLauncherConfiguration(
            LauncherConfiguration configuration,
            ManifestSourceKind sourceKind
    ) {
        this(configuration, sourceKind, Optional.empty());
    }

    public ResolvedLauncherConfiguration {
        Objects.requireNonNull(configuration, "configuration");
        Objects.requireNonNull(sourceKind, "sourceKind");
        Objects.requireNonNull(managedManifestUris, "managedManifestUris");

        if (sourceKind == ManifestSourceKind.MANAGED && managedManifestUris.isEmpty()) {
            throw new IllegalArgumentException(
                    "managedManifestUris must not be empty"
            );
        }

        if (managedManifestUris.isPresent()) {
            URI manifestUri = configuration.manifestUri();
            URI managedManifestUri = managedManifestUris.get().manifestUri();

            if (!manifestUri.equals(managedManifestUri)) {
                throw new IllegalArgumentException(
                        "manifestUri and managedManifestUri must be identical"
                );
            }
        }

        if (sourceKind != ManifestSourceKind.MANAGED && managedManifestUris.isPresent()) {
            throw new IllegalArgumentException(
                    "managedManifestUris must be empty"
            );
        }
    }
}
