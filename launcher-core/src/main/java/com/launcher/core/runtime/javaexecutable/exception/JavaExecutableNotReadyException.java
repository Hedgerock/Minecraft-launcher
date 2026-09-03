package com.launcher.core.runtime.javaexecutable.exception;

public class JavaExecutableNotReadyException extends RuntimeException {
    public JavaExecutableNotReadyException(String message) {
        super(message);
    }
}
