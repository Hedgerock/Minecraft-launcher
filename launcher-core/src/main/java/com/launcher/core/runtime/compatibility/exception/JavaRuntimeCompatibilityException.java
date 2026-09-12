package com.launcher.core.runtime.compatibility.exception;

import com.launcher.core.runtime.JavaRuntimeFailureException;
import com.launcher.core.runtime.JavaRuntimeFailureReason;

public final class JavaRuntimeCompatibilityException extends JavaRuntimeFailureException {

    public JavaRuntimeCompatibilityException(JavaRuntimeFailureReason reason, String message) {
        super(reason, message);
    }
}
