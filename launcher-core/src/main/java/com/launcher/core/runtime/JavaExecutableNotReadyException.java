package com.launcher.core.runtime;

public class JavaExecutableNotReadyException extends RuntimeException {
    public JavaExecutableNotReadyException(String message) {
        super(message);
    }
}
