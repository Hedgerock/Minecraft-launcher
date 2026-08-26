package com.launcher.model.manifest;

import java.util.Objects;

public record RuntimeLibraryMetadata(
        LibraryArtifactMetadata artifact
) {

    public RuntimeLibraryMetadata {
        Objects.requireNonNull(artifact, "artifact");
    }

}
