package com.launcher.core.operation.result;

import java.util.Optional;

public final class OperationResult {
    private static final OperationResult SUCCESS = new OperationResult(true, null);

    private final boolean success;
    private final String errorMessage;

    private OperationResult(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public static OperationResult success() {
        return SUCCESS;
    }

    public static OperationResult failure(String errorMessage) {
        return new OperationResult(false, errorMessage);
    }
    public
    Optional<String> errorMessage() {
        return Optional.ofNullable(errorMessage);
    }


}
