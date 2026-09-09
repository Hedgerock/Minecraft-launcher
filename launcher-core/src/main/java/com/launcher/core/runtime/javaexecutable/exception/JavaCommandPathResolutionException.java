package com.launcher.core.runtime.javaexecutable.exception;

public final class JavaCommandPathResolutionException extends JavaRuntimeFailureException {

    public JavaCommandPathResolutionException(
            JavaRuntimeFailureReason reason,
            String message
    ) {
        super(reason, message);
    }
}
