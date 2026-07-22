package com.launcher.core.result;

public final class FailureResult implements  Result {
    private final String message;
    public FailureResult(String message) {
        this.message = message;
    }
    @Override
    public boolean success() {
        return false;
    }

    public String getMessage() {
        return message;
    }
}
