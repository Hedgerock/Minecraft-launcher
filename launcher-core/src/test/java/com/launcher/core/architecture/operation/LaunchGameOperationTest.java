package com.launcher.core.architecture.operation;

import com.launcher.core.architecture.support.FailingGameService;
import com.launcher.core.architecture.support.fixture.OperationFactoryFixture;
import com.launcher.core.event.EventBus;
import com.launcher.core.execution.SequentialExecutionStrategy;
import com.launcher.core.game.GameLaunchPlan;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.impl.LaunchGameOperation;
import com.launcher.core.operation.result.OperationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LaunchGameOperationTest {

    private LaunchContext context;

    @BeforeEach
    void setUp() {
        context = OperationFactoryFixture.getContext();
    }

    @Test
    void should_return_failure_when_game_service_throws_exception() {
        context.setGameLaunchPlan(
                new GameLaunchPlan(
                        Path.of("game-directory"),
                        List.of("java", "TestMain")
                )
        );

        //given
        LaunchOperation launchOperation = new LaunchGameOperation(
                context,
                new SequentialExecutionStrategy(),
                new EventBus(),
                new FailingGameService()
        );

        //when
        OperationResult result = launchOperation.execute();

        //then
        assertFalse(result.isSuccess());
        assertEquals("Game launch failed", result.errorMessage().orElseThrow());
    }

}
