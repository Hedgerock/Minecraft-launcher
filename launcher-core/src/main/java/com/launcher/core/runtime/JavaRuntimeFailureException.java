package com.launcher.core.runtime;

import java.util.Objects;

public abstract class JavaRuntimeFailureException extends RuntimeException {
    private final JavaRuntimeFailureReason reason;

    protected JavaRuntimeFailureException(
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
