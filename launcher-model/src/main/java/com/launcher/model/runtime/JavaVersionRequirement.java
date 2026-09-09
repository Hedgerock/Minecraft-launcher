package com.launcher.model.runtime;

public record JavaVersionRequirement(
        int minimumMajorVersion
) {

    public JavaVersionRequirement {
        if (minimumMajorVersion <= 0) {
            throw new IllegalArgumentException("minimumMajorVersion must be positive");
        }
    }
}
