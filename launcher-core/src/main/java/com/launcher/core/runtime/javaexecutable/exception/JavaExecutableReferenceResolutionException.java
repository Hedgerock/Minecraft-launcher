package com.launcher.core.runtime.javaexecutable.exception;

import java.util.Objects;

public class JavaExecutableReferenceResolutionException extends RuntimeException {
    private final JavaRuntimeFailureReason reason;

    public JavaExecutableReferenceResolutionException(
            JavaRuntimeFailureReason reason,
            String message
    ) {
        super(message);
        this.reason = Objects.requireNonNull(reason, "reason");
    }

    public JavaRuntimeFailureReason getReason() {
        return reason;
    }
}
