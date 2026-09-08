package com.launcher.core.runtime.model;

import com.launcher.model.manifest.LaunchInfo;

import java.util.Objects;
import java.util.Optional;

public record JavaRuntimeSelectionRequest(
        LaunchInfo launchInfo,
        Optional<String> javaExecutableOverride
) {

    public JavaRuntimeSelectionRequest {
        Objects.requireNonNull(launchInfo, "launchInfo");
        Objects.requireNonNull(javaExecutableOverride, "javaExecutableOverride");
    }

    public static JavaRuntimeSelectionRequest fromManifest(LaunchInfo launchInfo) {
        return new JavaRuntimeSelectionRequest(
                launchInfo,
                Optional.empty()
        );
    }
}
