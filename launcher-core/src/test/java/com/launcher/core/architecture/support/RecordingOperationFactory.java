package com.launcher.core.architecture.support;

import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.OperationFactory;
import com.launcher.core.operation.OperationType;
import com.launcher.core.operation.RepairOperation;

public class RecordingOperationFactory implements OperationFactory {
    public static final String RECORDING_EVENT_NAME = "OperationFactory";

    private final RecordingEvents recordingEvents;

    public RecordingOperationFactory(RecordingEvents recordingEvents) {
        this.recordingEvents = recordingEvents;
    }

    @Override
    public LaunchOperation create(OperationType type, LaunchContext context, ExecutionStrategy executionStrategy) {
        recordingEvents.record(RECORDING_EVENT_NAME);

        return new RepairOperation(context, executionStrategy);
    }
}
