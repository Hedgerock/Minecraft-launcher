package com.launcher.core.result;

import com.launcher.core.operation.failure.OperationFailure;

import java.util.Objects;

public final class FailureResult implements Result {
    private final OperationFailure failure;

    public FailureResult(OperationFailure failure) {
        this.failure = Objects.requireNonNull(failure, "failure");
    }

    @Override
    public boolean success() {
        return false;
    }

    public String message() {
        return failure.message();
    }

    public OperationFailure failure() {
        return failure;
    }
}
