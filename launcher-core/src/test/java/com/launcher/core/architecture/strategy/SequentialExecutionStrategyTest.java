package com.launcher.core.architecture.strategy;

import com.launcher.core.architecture.support.recording.RecordingEvents;
import com.launcher.core.architecture.support.recording.RecordingLauncherTask;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.execution.SequentialExecutionStrategy;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.failure.OperationFailure;
import com.launcher.core.operation.failure.OperationFailureCode;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.task.LauncherTask;
import com.launcher.core.task.TaskResult;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SequentialExecutionStrategyTest {
    private final SequentialExecutionStrategy sequentialExecutionStrategy =
            new SequentialExecutionStrategy();

    @Test
    void should_preserve_task_failure_context() {
        //given
        RecordingEvents events = new RecordingEvents();
        OperationFailure failure = new OperationFailure(
                OperationFailureCode.UNKNOWN,
                "failure",
                Map.of("task", "test")
        );

        List<LauncherTask> tasks = List.of(
                new RecordingLauncherTask(
                        events,
                        "Task-1",
                        TaskResult.failure(failure)
                )
        );

        LaunchContext context = getContext();

        //when
        OperationResult result = sequentialExecutionStrategy.execute(tasks, context);

        //then
        assertFalse(result.isSuccess());
        assertEquals(failure, result.failure().orElseThrow());
    }

    @Test
    void should_execute_all_tasks_in_order() {
        //given

        RecordingEvents events = new RecordingEvents();
        List<LauncherTask> tasks = List.of(
                success(events, "Task-1"),
                success(events, "Task-2"),
                success(events, "Task-3")
        );
        LaunchContext context = getContext();
        //when
        sequentialExecutionStrategy.execute(tasks, context);
        //then
        assertEquals(
                List.of(
                        "Task-1",
                        "Task-2",
                        "Task-3"
                ),
                events.events()
        );
    }

    @Test
    void should_return_success_when_all_tasks_complete() {
        //given
        RecordingEvents events = new RecordingEvents();
        List<LauncherTask> tasks = List.of(
                success(events, "Task-1"),
                success(events, "Task-2"),
                success(events, "Task-3")
        );
        LaunchContext context = getContext();
        //when
        OperationResult result = sequentialExecutionStrategy.execute(tasks, context);
        //then
        assertTrue(result.isSuccess());
    }

    @Test
    void should_stop_execution_after_failure() {
        //given
        RecordingEvents events = new RecordingEvents();
        List<LauncherTask> tasks = List.of(
                success(events, "Task-1"),
                failure(events),
                success(events, "Task-3")
        );
        LaunchContext context = getContext();
        //when
        OperationResult result = sequentialExecutionStrategy.execute(tasks, context);

        //then
        assertEquals(
                List.of("Task-1", "Task-2"),
                events.events()
        );

        assertFalse(result.isSuccess());
    }

    private LaunchContext getContext() {
        return new LaunchContext(
                new LauncherConfiguration(
                        URI.create("currentPath"),
                        Path.of("")
                )
        );
    }

    private RecordingLauncherTask success(
            RecordingEvents events,
            String name
    ) {
        return new RecordingLauncherTask(
                events,
                name,
                TaskResult.success()
        );
    }

    private RecordingLauncherTask failure(
            RecordingEvents events
    ) {
        return new RecordingLauncherTask(
                events,
                "Task-2",
                TaskResult.failure("failure")
        );
    }
}
