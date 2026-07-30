package com.launcher.core.event.events;

import com.launcher.core.event.Event;
import com.launcher.core.operation.type.OperationType;

public record OperationCompletedEvent(
        OperationType operationType
) implements Event {
}
