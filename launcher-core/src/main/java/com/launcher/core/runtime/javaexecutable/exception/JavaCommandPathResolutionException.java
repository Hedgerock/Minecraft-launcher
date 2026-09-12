package com.launcher.core.runtime.javaexecutable.exception;

import com.launcher.core.runtime.JavaRuntimeFailureException;
import com.launcher.core.runtime.JavaRuntimeFailureReason;

public final class JavaCommandPathResolutionException extends JavaRuntimeFailureException {

    public JavaCommandPathResolutionException(
            JavaRuntimeFailureReason reason,
            String message
    ) {
        super(reason, message);
    }
}
