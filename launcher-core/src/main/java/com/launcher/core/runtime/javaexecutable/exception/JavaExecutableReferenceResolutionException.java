package com.launcher.core.runtime.javaexecutable.exception;

public final class JavaExecutableReferenceResolutionException extends JavaRuntimeFailureException {

    public JavaExecutableReferenceResolutionException(
            JavaRuntimeFailureReason reason,
            String message
    ) {
        super(reason, message);
    }
}
