package com.launcher.model.manifest.natives;

import com.launcher.model.manifest.LibraryEntry;

import java.util.Objects;

public record SelectedNativeArtifact(
        LibraryEntry artifact,
        NativeExtractionRules extractionRules
) {

    public SelectedNativeArtifact {
        Objects.requireNonNull(artifact, "artifact");
        Objects.requireNonNull(extractionRules, "extractionRules");
    }

}
