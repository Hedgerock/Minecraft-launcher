package com.launcher.core.runtime.compatibility.model;

import com.launcher.model.runtime.JavaExecutableReference;
import com.launcher.model.runtime.JavaVersionRequirement;

import java.util.Objects;

public record JavaRuntimeCompatibilityRequest(
        JavaExecutableReference resolvedJavaExecutableReference,
        JavaVersionRequirement javaVersionRequirement
) {

    public JavaRuntimeCompatibilityRequest {
        Objects.requireNonNull(resolvedJavaExecutableReference, "resolvedJavaExecutableReference");
        Objects.requireNonNull(javaVersionRequirement, "javaVersionRequirement");
    }
}
