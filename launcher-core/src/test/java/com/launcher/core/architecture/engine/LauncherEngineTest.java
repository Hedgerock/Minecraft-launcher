package com.launcher.core.architecture.engine;

import com.launcher.core.LauncherEngine;
import com.launcher.core.architecture.support.RecordingOperationManager;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.event.EventBus;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.operation.type.OperationType;
import com.launcher.core.state.LauncherState;
import com.launcher.core.state.LauncherStateMachine;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LauncherEngineTest {

    private LauncherConfiguration configuration() {
        return new LauncherConfiguration(
                URI.create("currentPath"),
                Path.of("")
        );
    }

    private LauncherStateMachine stateMachine() {
        return new LauncherStateMachine(new EventBus());
    }


    @Test
    void should_execute_load_manifest_operation_when_launch_starts() {
        //given
        LauncherStateMachine stateMachine = stateMachine();
        RecordingOperationManager operationManager = new RecordingOperationManager(OperationResult.success());
        LauncherEngine engine = new LauncherEngine(stateMachine, operationManager);

        //when
        engine.launch(configuration());

        //then
        assertEquals(
                OperationType.LOAD_MANIFEST,
                operationManager.getExecutedOperationType()
        );

        assertNotNull(operationManager.getReceivedContext());
    }

    @Test
    void should_transition_to_running_when_operation_completed_successfully() {
        //given
        LauncherStateMachine stateMachine = stateMachine();
        RecordingOperationManager operationManager = new RecordingOperationManager(OperationResult.success());
        LauncherEngine engine = new LauncherEngine(stateMachine, operationManager);

        //when
        engine.launch(configuration());
        //then
        assertEquals(
                LauncherState.RUNNING,
                stateMachine.getCurrentState()
        );
    }

    @Test
    void should_transition_to_failed_when_operation_failed() {
        //given
        LauncherStateMachine stateMachine = stateMachine();
        RecordingOperationManager operationManager =
                new RecordingOperationManager(OperationResult.failure("load manifest failed"));
        LauncherEngine engine = new LauncherEngine(stateMachine, operationManager);

        //when
        engine.launch(configuration());

        //then
        assertEquals(
                LauncherState.FAILED,
                stateMachine.getCurrentState()
        );
    }
}
