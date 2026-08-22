package com.launcher.model.manifest;

import java.util.Objects;

public record ResourceEntry(
        String path,
        String sha256,
        long size,
        String url
) {

    public ResourceEntry {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(sha256, "sha256");
        Objects.requireNonNull(url, "url");

        validateField(path.isBlank(), "path must not be blank");
        validateField(sha256.isBlank(), "sha256 must not be blank");
        validateField(url.isBlank(), "url must not be blank");
        validateField(size < 0, "size must be positive");

    }

    private void validateField(boolean condition, String message) {
        if (condition) {
            throw new IllegalArgumentException(message);
        }
    }

}
