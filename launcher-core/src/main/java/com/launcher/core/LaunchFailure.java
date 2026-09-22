package com.launcher.core;

import com.launcher.core.operation.failure.OperationFailure;
import com.launcher.core.operation.type.OperationType;

import java.util.Objects;
import java.util.Optional;

public final class LaunchFailure {
    private final OperationType operationType;
    private final OperationFailure operationFailure;
    private final String message;

    private LaunchFailure(
            OperationType operationType,
            OperationFailure operationFailure
    ) {
        this.operationType = Objects.requireNonNull(operationType, "operationType");
        this.operationFailure = Objects.requireNonNull(operationFailure, "operationFailure");
        this.message = operationFailure.message();
    }

    private LaunchFailure(String message) {
        Objects.requireNonNull(message, "message");

        if (message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }

        this.operationType = null;
        this.operationFailure = null;
        this.message = message;
    }

    public static LaunchFailure operation(
            OperationType operationType,
            OperationFailure operationFailure
    ) {
        return new LaunchFailure(operationType, operationFailure);
    }

    public static LaunchFailure lifecycle(String message) {
        return new LaunchFailure(message);
    }

    public String message() {
        return message;
    }

    public Optional<OperationType> operationType() {
        return Optional.ofNullable(operationType);
    }

    public Optional<OperationFailure> operationFailure() {
        return Optional.ofNullable(operationFailure);
    }
}
