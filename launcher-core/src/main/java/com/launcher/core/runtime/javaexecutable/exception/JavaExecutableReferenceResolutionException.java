package com.launcher.core.runtime.javaexecutable.exception;

import com.launcher.core.runtime.JavaRuntimeFailureException;
import com.launcher.core.runtime.JavaRuntimeFailureReason;

public final class JavaExecutableReferenceResolutionException extends JavaRuntimeFailureException {

    public JavaExecutableReferenceResolutionException(
            JavaRuntimeFailureReason reason,
            String message
    ) {
        super(reason, message);
    }
}
