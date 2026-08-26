package com.launcher.api.manifest.mapper.dto;

import com.launcher.model.manifest.RuntimeLibraryMetadata;

public record LibraryEntryJson(
        String path,
        String sha256,
        long size,
        String url
) {
    RuntimeLibraryMetadata toRuntimeLibraryMetadata() {
        return new RuntimeLibraryMetadata(
                path,
                sha256,
                size,
                url
        );
    }
}
