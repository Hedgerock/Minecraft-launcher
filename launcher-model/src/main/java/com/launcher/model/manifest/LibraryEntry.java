package com.launcher.model.manifest;

import java.util.Objects;

public record LibraryEntry(
        String path
) {

    public LibraryEntry {
        Objects.requireNonNull(path, "path");

        if (path.isBlank()) {
            throw new IllegalArgumentException("path must not be blank");
        }

    }

}
