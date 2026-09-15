package com.launcher.core.operation.failure;

import java.util.Map;
import java.util.Objects;

public record OperationFailure(
        OperationFailureCode code,
        String message,
        Map<String, String> details
) {

    public OperationFailure {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(details, "details");

        if (message.isBlank()) {
            throw new IllegalArgumentException("message cannot be blank");
        }

        details = Map.copyOf(details);
    }
}
