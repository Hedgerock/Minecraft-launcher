package com.launcher.core.runtime.compatibility.model;

import com.launcher.model.runtime.JavaRuntimeVersion;
import com.launcher.model.runtime.JavaVersionRequirement;

import java.util.Objects;

public record JavaRuntimeCompatibilityRequest(
        JavaRuntimeVersion javaRuntimeVersion,
        JavaVersionRequirement javaVersionRequirement
) {

    public JavaRuntimeCompatibilityRequest {
        Objects.requireNonNull(javaRuntimeVersion, "javaRuntimeVersion");
        Objects.requireNonNull(javaVersionRequirement, "javaVersionRequirement");
    }
}
