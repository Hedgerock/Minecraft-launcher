package com.launcher.core.architecture.support;

import com.launcher.core.runtime.JavaRuntimeFailureReason;
import com.launcher.core.runtime.compatibility.JavaRuntimeCompatibilityChecker;
import com.launcher.core.runtime.compatibility.exception.JavaRuntimeCompatibilityException;
import com.launcher.core.runtime.compatibility.model.JavaRuntimeCompatibilityRequest;

public final class FailingJavaRuntimeCompatibilityChecker implements JavaRuntimeCompatibilityChecker {
    private final int requiredJavaVersion;

    public FailingJavaRuntimeCompatibilityChecker(int requiredJavaVersion) {
        this.requiredJavaVersion = requiredJavaVersion;
    }

    @Override
    public void checkCompatible(JavaRuntimeCompatibilityRequest request) {
        throw new JavaRuntimeCompatibilityException(
                JavaRuntimeFailureReason.INCOMPATIBLE_JAVA_VERSION,
                "Java runtime version 8 does not satisfy required Java version " + requiredJavaVersion
        );
    }
}
