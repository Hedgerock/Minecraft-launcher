package com.launcher.core.runtime.detection.model;

import com.launcher.model.runtime.JavaExecutableReference;

import java.util.Objects;

public record JavaRuntimeVersionDetectionRequest(
        JavaExecutableReference resolvedJavaExecutableReference
) {

    public JavaRuntimeVersionDetectionRequest {
        Objects.requireNonNull(resolvedJavaExecutableReference, "resolvedJavaExecutableReference");
    }
}
