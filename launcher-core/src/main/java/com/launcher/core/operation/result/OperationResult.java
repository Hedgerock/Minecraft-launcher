package com.launcher.core.operation.result;

public record OperationResult(
        boolean isSuccess
) {

    public static OperationResult success() {
        return new OperationResult(true);
    }

    public static OperationResult failure(String message) {
        System.out.println(message);

        return new OperationResult(false);
    }
}
