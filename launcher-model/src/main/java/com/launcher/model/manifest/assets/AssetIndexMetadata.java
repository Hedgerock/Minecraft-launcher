package com.launcher.model.manifest.assets;

import java.util.Objects;
import java.util.regex.Pattern;

public record AssetIndexMetadata(
        String id,
        String path,
        String sha256,
        long size,
        String url
) {
    private static final Pattern SAFE_ID =
            Pattern.compile("[A-Za-z0-9._-]+");

    private static final Pattern RESERVED_WINDOWS_NAME =
            Pattern.compile("(?i)(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\..*)?");

    private static void validateId(String id) {
        if (!SAFE_ID.matcher(id).matches()
                || id.equals("..")
                || id.endsWith(".")
                || RESERVED_WINDOWS_NAME.matcher(id).matches()
        ) {
            throw new IllegalArgumentException("Invalid assets index id");
        }
    }

    public AssetIndexMetadata {
        Objects.requireNonNull(id, "id");
        notBlank(id, "id");

        validateId(id);
        Objects.requireNonNull(path, "path");

        notBlank(path, "path");

        String expectedPath = "assets/indexes/" + id + ".json";

        if (!expectedPath.equals(path)) {
            throw new IllegalArgumentException(
                    "Assets index path must match id"
            );
        }

        if (size < 0) {
            throw new IllegalArgumentException("size must not be negative");
        }

        Objects.requireNonNull(sha256, "sha256");
        Objects.requireNonNull(url, "url");

        notBlank(sha256, "sha256");
        notBlank(url, "url");
    }

    private void notBlank(String value, String fieldName) {
        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    "%s must not be blank".formatted(fieldName)
            );
        }
    }
}
