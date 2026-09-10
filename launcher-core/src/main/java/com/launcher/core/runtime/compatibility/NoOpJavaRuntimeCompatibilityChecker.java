package com.launcher.core.runtime.compatibility;

import com.launcher.core.runtime.compatibility.model.JavaRuntimeCompatibilityRequest;

import java.util.Objects;

public final class NoOpJavaRuntimeCompatibilityChecker implements JavaRuntimeCompatibilityChecker {

    @Override
    public void checkCompatible(JavaRuntimeCompatibilityRequest request) {
        Objects.requireNonNull(request, "request");
    }
}
