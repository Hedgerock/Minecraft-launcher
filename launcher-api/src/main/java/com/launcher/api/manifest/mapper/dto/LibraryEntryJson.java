package com.launcher.api.manifest.mapper.dto;

import com.launcher.model.manifest.LibraryEntry;

public record LibraryEntryJson(
        String path
) {
    LibraryEntry toLibraryEntry() {
        return new LibraryEntry(path);
    }
}
