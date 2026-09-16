package com.launcher.model.manifest.assets;

import java.util.Objects;

public record AssetEntry(
        String path,
        String sha256,
        long size,
        String url
) {

    public AssetEntry {
        validateField(path, "path");
        validateField(sha256, "sha256");
        validateField(url, "url");
        validateField(size);
    }

    private void validateField(String field, String message) {
        Objects.requireNonNull(field, message);

        if (field.isBlank()) {
            throw new IllegalArgumentException(message + " must not be blank");
        }
    }

    private void validateField(long field) {
        if (field < 0) {
            throw new IllegalArgumentException("size must be positive");
        }
    }
}
