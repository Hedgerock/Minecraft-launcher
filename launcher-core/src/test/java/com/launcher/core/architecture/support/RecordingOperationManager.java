package com.launcher.core.architecture.support;

import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.OperationManager;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.operation.type.OperationType;

public class RecordingOperationManager implements OperationManager {

    private final OperationResult result;
    private OperationType executedOperationType;
    private LaunchContext receivedContext;

    public RecordingOperationManager(OperationResult result) {
        this.result = result;
    }

    @Override
    public OperationResult execute(OperationType type, LaunchContext context) {
        this.executedOperationType = type;
        this.receivedContext = context;

        return result;
    }

    public OperationType getExecutedOperationType() {
        return executedOperationType;
    }

    public LaunchContext getReceivedContext() {
        return receivedContext;
    }
}
