package com.launcher.api.manifest.mapper.dto;

import com.launcher.model.manifest.FileEntry;

public record FileEntryJson(
        String path,
        String sha256,
        long size,
        String url
) {

    FileEntry toFileEntry() {
        return new FileEntry(
                path,
                sha256,
                size,
                url
        );
    }

}
