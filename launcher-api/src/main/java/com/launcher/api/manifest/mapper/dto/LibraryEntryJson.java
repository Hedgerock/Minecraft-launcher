package com.launcher.api.manifest.mapper.dto;

import com.launcher.model.manifest.LibraryEntry;

public record LibraryEntryJson(
        String path,
        String sha256,
        long size,
        String url
) {
    LibraryEntry toLibraryEntry() {
        return new LibraryEntry(
                path,
                sha256,
                size,
                url
        );
    }
}
