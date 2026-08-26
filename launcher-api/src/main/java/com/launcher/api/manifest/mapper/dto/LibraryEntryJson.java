package com.launcher.api.manifest.mapper.dto;

import com.launcher.model.manifest.LibraryArtifactMetadata;
import com.launcher.model.manifest.RuntimeLibraryMetadata;

public record LibraryEntryJson(
        String path,
        String sha256,
        long size,
        String url
) {
    RuntimeLibraryMetadata toRuntimeLibraryMetadata() {
        return new RuntimeLibraryMetadata(
                toLibraryArtifactMetadata()
        );
    }

    private LibraryArtifactMetadata toLibraryArtifactMetadata() {
        return new LibraryArtifactMetadata(
                path,
                sha256,
                size,
                url
        );
    }
}
