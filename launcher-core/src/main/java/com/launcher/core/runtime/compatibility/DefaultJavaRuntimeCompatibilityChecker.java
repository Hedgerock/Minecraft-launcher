package com.launcher.core.runtime.compatibility;

import com.launcher.core.runtime.JavaRuntimeFailureReason;
import com.launcher.core.runtime.compatibility.exception.JavaRuntimeCompatibilityException;
import com.launcher.core.runtime.compatibility.model.JavaRuntimeCompatibilityRequest;

import java.util.Objects;

public final class DefaultJavaRuntimeCompatibilityChecker implements JavaRuntimeCompatibilityChecker {
    private static final String TEMPLATE_OF_FAILURE_MESSAGE =
            "Java runtime version %d does not satisfy required Java version %d";

    @Override
    public void checkCompatible(JavaRuntimeCompatibilityRequest request) {
        Objects.requireNonNull(request, "request");

        int runtimeMajorVersion = request.javaRuntimeVersion().majorVersion();
        int requiredMajorVersion = request.javaVersionRequirement().minimumMajorVersion();

        if (runtimeMajorVersion < requiredMajorVersion) {
            throw new JavaRuntimeCompatibilityException(
                    JavaRuntimeFailureReason.INCOMPATIBLE_JAVA_VERSION,
                    String.format(TEMPLATE_OF_FAILURE_MESSAGE, runtimeMajorVersion, requiredMajorVersion)
            );
        }
    }
}
