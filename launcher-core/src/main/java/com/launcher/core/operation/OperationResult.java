package com.launcher.core.operation;

public record OperationResult(
        boolean isSuccess
) {

    public static OperationResult success() {
        return new OperationResult(true);
    }

    public static OperationResult failure() {
        return new OperationResult(false);
    }
}
