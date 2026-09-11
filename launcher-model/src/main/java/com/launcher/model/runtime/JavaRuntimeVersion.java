package com.launcher.model.runtime;

public record JavaRuntimeVersion(
        int majorVersion
) {

    public JavaRuntimeVersion {
        if (majorVersion <= 0) {
            throw new IllegalArgumentException("majorVersion must be positive");
        }
    }
}
