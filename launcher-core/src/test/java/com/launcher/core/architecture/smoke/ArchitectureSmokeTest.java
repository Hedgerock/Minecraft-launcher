package com.launcher.core.architecture.smoke;

import com.launcher.core.architecture.support.recording.RecordingEvents;
import com.launcher.core.architecture.support.recording.RecordingExecutionStrategy;
import com.launcher.core.architecture.support.recording.RecordingOperationFactory;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.event.EventBus;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.DefaultOperationManager;
import com.launcher.core.operation.OperationManager;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.operation.type.OperationType;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchitectureSmokeTest {

    private LaunchContext getContext() {
        return new LaunchContext(
                new LauncherConfiguration(
                        URI.create("currentPath"),
                        Path.of("")
                )
        );
    }

    private OperationManager getDefaultOperationManager(RecordingEvents events) {
        RecordingExecutionStrategy strategy = new RecordingExecutionStrategy(events);
        RecordingOperationFactory factory = new RecordingOperationFactory(
                events,
                new EventBus()
        );
        return new DefaultOperationManager(factory, strategy);
    }

    @Test
    void should_contains_new_repair_operation() {
        //given
        RecordingEvents events = new RecordingEvents();
        LaunchContext launchContext = getContext();
        OperationManager manager = getDefaultOperationManager(events);
        //when
        manager.execute(OperationType.REPAIR, launchContext);

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
        LaunchContext launchContext = getContext();
        OperationManager manager = getDefaultOperationManager(events);
        //when
        manager.execute(OperationType.REPAIR, launchContext);

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
    void should_execute_repair_operation_successfully() {
        //given
        RecordingEvents events = new RecordingEvents();
        LaunchContext launchContext = getContext();
        OperationManager manager = getDefaultOperationManager(events);
        //when
        OperationResult result = manager.execute(OperationType.REPAIR, launchContext);

        //then

        assertTrue(result.isSuccess());
    }

}
