package com.launcher.core.architecture.smoke;

import com.launcher.core.architecture.support.RecordingEvents;
import com.launcher.core.architecture.support.RecordingExecutionStrategy;
import com.launcher.core.architecture.support.RecordingOperationFactory;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.OperationManager;
import com.launcher.core.operation.OperationResult;
import com.launcher.core.operation.OperationType;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchitectureSmokeTest {

    @Test
    void should_contains_new_repair_operation() {
        //given
        RecordingEvents events = new RecordingEvents();
        RecordingExecutionStrategy strategy = new RecordingExecutionStrategy(events);
        RecordingOperationFactory factory = new RecordingOperationFactory(events);
        LaunchContext launchContext = new LaunchContext(
                new LauncherConfiguration(
                        URI.create("currentPath"),
                        Path.of("")
                )
        );
        OperationManager manager = new OperationManager(factory, strategy, launchContext);
        //when
        manager.execute(OperationType.REPAIR);

        //then

        assertEquals(
                RecordingOperationFactory.RECORDING_EVENT_NAME,
                events.events().getFirst()
        );
    }

    @Test
    void should_return_sequential_list_of_the_components() {
        //given
        RecordingEvents events = new RecordingEvents();
        RecordingExecutionStrategy strategy = new RecordingExecutionStrategy(events);
        RecordingOperationFactory factory = new RecordingOperationFactory(events);
        LaunchContext launchContext = new LaunchContext(
                new LauncherConfiguration(
                        URI.create("currentPath"),
                        Path.of("")
                )
        );
        OperationManager manager = new OperationManager(factory, strategy, launchContext);
        //when
        manager.execute(OperationType.REPAIR);

        //then

        assertEquals(
                List.of(
                        RecordingOperationFactory.RECORDING_EVENT_NAME,
                        RecordingExecutionStrategy.STRATEGY_NAME
                ),
                events.events()
        );
    }

    @Test
    void should_execute_repair_operation_pipeline() {
        //given
        RecordingEvents events = new RecordingEvents();
        RecordingExecutionStrategy strategy = new RecordingExecutionStrategy(events);
        RecordingOperationFactory factory = new RecordingOperationFactory(events);
        LaunchContext launchContext = new LaunchContext(
                new LauncherConfiguration(
                        URI.create("currentPath"),
                        Path.of("")
                )
        );
        OperationManager manager = new OperationManager(factory, strategy, launchContext);
        //when
        OperationResult result = manager.execute(OperationType.REPAIR);

        //then

        assertTrue(result.isSuccess());
    }

}
