package com.launcher.core.event.events;

import com.launcher.core.event.Event;
import com.launcher.core.operation.failure.OperationFailure;
import com.launcher.core.operation.type.OperationType;

import java.util.Objects;

public record OperationFailedEvent(
        OperationType operationType,
        OperationFailure operationFailure
) implements Event {

    public OperationFailedEvent {
        Objects.requireNonNull(operationType, "operationType");
        Objects.requireNonNull(operationFailure, "operationFailure");
    }

    public String errorMessage() {
        return operationFailure.message();
    }
}
