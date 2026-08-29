package com.launcher.model.manifest;

import java.util.Objects;

public record ManifestLoadResult(
        Manifest manifest,
        RuntimeLibrarySelection runtimeLibrarySelection
) {

    public ManifestLoadResult {
        Objects.requireNonNull(manifest, "manifest");
        Objects.requireNonNull(runtimeLibrarySelection, "runtimeLibrarySelection");
    }

}
