package com.launcher.preparer.model;

import java.util.Objects;
import java.util.regex.Pattern;

public record VerifiedVanillaAssetIndex(
        VanillaAssetIndex index,
        String sha256,
        long size
) {

    private static final Pattern SHA256_PATTERN = Pattern.compile("^[0-9a-f]{64}$");

    public VerifiedVanillaAssetIndex {
        Objects.requireNonNull(index, "index");
        Objects.requireNonNull(sha256, "sha256");

        if (!SHA256_PATTERN.matcher(sha256).matches()) {
            throw new IllegalArgumentException("sha256 is not a valid SHA-256 hash");
        }

        if (size < 0) {
            throw new IllegalArgumentException("size must not be negative");
        }
    }
}
