package com.launcher.natives.exception;

public class NativeExtractionException extends RuntimeException {
    public NativeExtractionException(String message, Throwable cause) {
        super(message, cause);
    }

    public NativeExtractionException(String message) {
        super(message);
    }
}
