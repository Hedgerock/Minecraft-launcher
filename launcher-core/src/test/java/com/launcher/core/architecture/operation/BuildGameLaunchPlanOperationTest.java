package com.launcher.core.architecture.operation;

import com.launcher.core.architecture.support.fixture.OperationFactoryFixture;
import com.launcher.core.architecture.support.recording.RecordingDirectoryProvider;
import com.launcher.core.event.EventBus;
import com.launcher.core.execution.SequentialExecutionStrategy;
import com.launcher.core.game.GameLaunchPlanBuilder;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.impl.BuildGameLaunchPlanOperation;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.model.manifest.LoaderInfo;
import com.launcher.model.manifest.Manifest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BuildGameLaunchPlanOperationTest {
    private LaunchContext context;

    @BeforeEach
    void setUp() {
        context = OperationFactoryFixture.getContext();
    }

    @Test
    void should_return_failure_when_build_game_launch_plan_failed() {
        //given
        context.setManifest(
                new Manifest(
                        "1.12.2",
                        new LoaderInfo("fabric", "0.16.10"),
                        List.of(),
                        null
                )
        );

        LaunchOperation launchOperation = new BuildGameLaunchPlanOperation(
                context,
                new SequentialExecutionStrategy(),
                new EventBus(),
                new GameLaunchPlanBuilder(
                        new RecordingDirectoryProvider()
                )
        );

        //when
        OperationResult result = launchOperation.execute();

        //then
        assertFalse(result.isSuccess());
        assertTrue(result.errorMessage().orElseThrow().contains("Launch info not available"));
        assertNull(context.getGameLaunchPlan());

    }

}
