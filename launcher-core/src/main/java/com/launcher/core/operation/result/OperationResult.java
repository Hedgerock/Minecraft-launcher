package com.launcher.core.operation.result;

import com.launcher.core.operation.failure.OperationFailure;
import com.launcher.core.operation.failure.OperationFailureCode;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class OperationResult {
    private static final OperationResult SUCCESS = new OperationResult(true, null);

    private final boolean success;
    private final OperationFailure failure;

    private OperationResult(boolean success, OperationFailure failure) {
        this.success = success;
        this.failure = failure;
    }

    public static OperationResult success() {
        return SUCCESS;
    }

    public static OperationResult failure(String errorMessage) {
        return new OperationResult(
                false,
                new OperationFailure(
                        OperationFailureCode.UNKNOWN,
                        errorMessage,
                        Map.of()
                )
        );
    }

    public static OperationResult failure(OperationFailure failure) {
        Objects.requireNonNull(failure, "failure");

        return new OperationResult(false, failure);
    }

    public Optional<OperationFailure> failure() {
        return Optional.ofNullable(failure);
    }

    public boolean isSuccess() {
        return success;
    }

    public Optional<String> errorMessage() {
        return failure().map(OperationFailure::message);
    }
}
