package com.launcher.core.architecture.support;

import com.launcher.core.event.EventBus;
import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.factory.OperationFactory;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.operation.impl.RepairOperation;

public class RecordingOperationFactory implements OperationFactory {
    public static final String RECORDING_EVENT_NAME = "OperationFactory";

    private final RecordingEvents recordingEvents;
    private final EventBus eventBus;

    public RecordingOperationFactory(
            RecordingEvents recordingEvents,
            EventBus eventBus
    ) {
        this.recordingEvents = recordingEvents;
        this.eventBus = eventBus;
    }

    @Override
    public LaunchOperation create(
            OperationType type,
            LaunchContext context,
            ExecutionStrategy executionStrategy
    ) {
        recordingEvents.record(RECORDING_EVENT_NAME);

        return new RepairOperation(context, executionStrategy, eventBus);
    }
}
