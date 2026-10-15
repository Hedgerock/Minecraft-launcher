package com.launcher.preparer.model;

import java.util.Objects;
import java.util.regex.Pattern;

public record VerifiedVanillaAssetObject(
        VanillaAssetObject object,
        String sha256
) {
    private static final Pattern SHA_256_PATTERN = Pattern.compile("^[0-9a-f]{64}$");

    public VerifiedVanillaAssetObject {
        Objects.requireNonNull(object, "object");
        Objects.requireNonNull(sha256, "sha256");

        if (!SHA_256_PATTERN.matcher(sha256).matches()) {
            throw new IllegalArgumentException("sha256 is not a valid SHA-256 hash");
        }
    }
}
