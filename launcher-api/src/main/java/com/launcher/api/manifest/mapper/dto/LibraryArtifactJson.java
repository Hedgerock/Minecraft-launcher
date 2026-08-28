package com.launcher.api.manifest.mapper.dto;

import com.launcher.model.manifest.LibraryArtifactMetadata;

public record LibraryArtifactJson(
        String path,
        String sha256,
        long size,
        String url
) {

    LibraryArtifactMetadata toLibraryArtifactMetadata() {
        return new LibraryArtifactMetadata(
                path,
                sha256,
                size,
                url
        );
    }

}
