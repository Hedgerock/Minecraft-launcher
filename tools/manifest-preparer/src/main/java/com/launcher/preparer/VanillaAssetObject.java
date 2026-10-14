package com.launcher.preparer;

import java.util.Objects;
import java.util.regex.Pattern;

public record VanillaAssetObject(
        String sha1,
        long size
) {
    private static final Pattern SHA1_PATTERN = Pattern.compile("^[0-9a-f]{40}$");

    public VanillaAssetObject {
        Objects.requireNonNull(sha1, "sha1");

        if (!SHA1_PATTERN.matcher(sha1).matches()) {
            throw new IllegalArgumentException(
                    "sha1 must contain 40 lowercase hexadecimal characters"
            );
        }

        if (size < 0) {
            throw new IllegalArgumentException("size must not be negative");
        }
    }
}
