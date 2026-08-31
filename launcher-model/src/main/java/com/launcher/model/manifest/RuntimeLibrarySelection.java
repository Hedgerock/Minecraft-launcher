package com.launcher.model.manifest;

import com.launcher.model.manifest.natives.SelectedNativeArtifact;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public record RuntimeLibrarySelection(
        List<LibraryEntry> libraries,
        List<SelectedNativeArtifact> nativeArtifacts
) {

    public RuntimeLibrarySelection {
        Objects.requireNonNull(libraries, "libraries");
        Objects.requireNonNull(nativeArtifacts, "nativeArtifacts");

        libraries = List.copyOf(libraries);
        nativeArtifacts = List.copyOf(nativeArtifacts);
    }

    public boolean hasNativeArtifacts() {
        return !nativeArtifacts.isEmpty();
    }

    public List<LibraryEntry> selectedArtifacts() {
        return Stream.concat(
                libraries.stream(),
                nativeArtifacts.stream().map(SelectedNativeArtifact::artifact)
        ).toList();
    }

}
