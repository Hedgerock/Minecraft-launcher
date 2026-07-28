package com.launcher.core.architecture.support;

import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.operation.OperationResult;
import com.launcher.core.task.LauncherTask;

import java.util.List;

public class RecordingExecutionStrategy implements ExecutionStrategy {
    public static final String STRATEGY_NAME = "RecordingExecutionStrategy";

    private final RecordingEvents recordingEvents;

    public RecordingExecutionStrategy(RecordingEvents recordingEvents) {
        this.recordingEvents = recordingEvents;
    }

    @Override
    public OperationResult execute(List<LauncherTask> tasks) {

        recordingEvents.record(STRATEGY_NAME);

        return OperationResult.success();
    }
}
