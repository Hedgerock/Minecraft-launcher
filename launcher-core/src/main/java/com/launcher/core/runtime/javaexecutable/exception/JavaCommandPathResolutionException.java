package com.launcher.core.runtime.javaexecutable.exception;

import java.util.Objects;

public class JavaCommandPathResolutionException extends RuntimeException {
    private final JavaRuntimeFailureReason reason;

    public JavaCommandPathResolutionException(
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
