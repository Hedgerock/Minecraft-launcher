package com.launcher.core.architecture.support.recording;

import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.task.LauncherTask;

import java.util.List;

public final class RecordingExecutionStrategy implements ExecutionStrategy {
    public static final String STRATEGY_NAME = "RecordingExecutionStrategy";

    private final RecordingEvents recordingEvents;

    public RecordingExecutionStrategy(RecordingEvents recordingEvents) {
        this.recordingEvents = recordingEvents;
    }

    @Override
    public OperationResult execute(List<LauncherTask> tasks, LaunchContext context) {

        recordingEvents.record(STRATEGY_NAME);

        return OperationResult.success();
    }
}
