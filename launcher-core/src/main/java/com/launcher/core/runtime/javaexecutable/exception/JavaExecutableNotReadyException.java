package com.launcher.core.runtime.javaexecutable.exception;

public final class JavaExecutableNotReadyException extends JavaRuntimeFailureException {

    public JavaExecutableNotReadyException(
            JavaRuntimeFailureReason reason,
            String message
    ) {
        super(reason, message);
    }
}
