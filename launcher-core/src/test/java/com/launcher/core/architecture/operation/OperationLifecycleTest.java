package com.launcher.core.architecture.operation;

import com.launcher.core.architecture.support.FailingLifecycleOperation;
import com.launcher.core.architecture.support.LifecycleRecordingOperation;
import com.launcher.core.architecture.support.RecordingEvents;
import com.launcher.core.architecture.support.RecordingExecutionStrategy;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.result.OperationResult;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OperationLifecycleTest {

    private LaunchContext getContext() {
        return new LaunchContext(
                new LauncherConfiguration(
                        URI.create("currentPath"),
                        Path.of("")
                )
        );
    }

    @Test
    void should_finalize_operation_when_lifecycle_failed() {
        //given
        RecordingEvents events = new RecordingEvents();
        LaunchContext context = getContext();
        LaunchOperation operation = new FailingLifecycleOperation(
                context,
                new RecordingExecutionStrategy(events),
                events
        );

        //when
        OperationResult result = operation.execute();

        //then
        assertEquals(
                List.of(
                        FailingLifecycleOperation.BEFORE_EXECUTE,
                        FailingLifecycleOperation.CREATE_TASK,
                        FailingLifecycleOperation.FINALIZE_OPERATION
                ),
                events.events()
        );

        assertFalse(result.isSuccess());
    }

    @Test
    void should_execute_operation_lifecycle_in_order() {
        //given
        RecordingEvents events = new RecordingEvents();
        LaunchContext context = getContext();
        LaunchOperation operation = new LifecycleRecordingOperation(
                context,
                new RecordingExecutionStrategy(events),
                events
        );
        //when

        OperationResult result = operation.execute();

        //then

        assertEquals(
                List.of(
                        LifecycleRecordingOperation.BEFORE_EXECUTE,
                        LifecycleRecordingOperation.CREATE_TASK,
                        RecordingExecutionStrategy.STRATEGY_NAME,
                        LifecycleRecordingOperation.AFTER_EXECUTE,
                        LifecycleRecordingOperation.FINALIZE_OPERATION
                ),
                events.events()
        );

        assertTrue(result.isSuccess());
    }

}
