package com.launcher.core.architecture.game;

import com.launcher.core.architecture.support.fixture.OperationFactoryFixture;
import com.launcher.core.architecture.support.recording.RecordingManifestService;
import com.launcher.core.game.BuildGameLaunchPlanTask;
import com.launcher.core.game.GameLaunchPlanBuilder;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.manifest.ManifestService;
import com.launcher.core.result.FailureResult;
import com.launcher.core.result.Result;
import com.launcher.core.result.SuccessResult;
import com.launcher.core.state.LauncherState;
import com.launcher.model.manifest.Manifest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuildGameLaunchPlanTaskTest {
    private LaunchContext context;
    private final ManifestService manifestService = new RecordingManifestService();

    @BeforeEach
    void setUp() {
        context = OperationFactoryFixture.getContext();
    }

    @Test
    void should_return_failure_when_manifest_not_loaded() {
        //given
        GameLaunchPlanBuilder gameLaunchPlanBuilder = OperationFactoryFixture.gameLaunchBuilder;
        BuildGameLaunchPlanTask task = new BuildGameLaunchPlanTask(gameLaunchPlanBuilder);

        //when
        Result result = task.execute(context);

        //then
        assertInstanceOf(FailureResult.class, result);
    }

    @Test
    void should_return_build_game_launch_plan_state() {
        //given
        GameLaunchPlanBuilder gameLaunchPlanBuilder = OperationFactoryFixture.gameLaunchBuilder;
        BuildGameLaunchPlanTask task = new BuildGameLaunchPlanTask(gameLaunchPlanBuilder);

        //then
        assertEquals(LauncherState.BUILDING_GAME_LAUNCH_PLAN, task.state());
    }

    @Test
    void should_return_success_result_when_game_launch_plan_is_built() {
        //given
        GameLaunchPlanBuilder gameLaunchPlanBuilder = OperationFactoryFixture.gameLaunchBuilder;
        BuildGameLaunchPlanTask task = new BuildGameLaunchPlanTask(gameLaunchPlanBuilder);
        Manifest manifest = manifestService.loadManifest();
        context.setManifest(manifest);

        //when
        Result result = task.execute(context);

        //then
        assertInstanceOf(SuccessResult.class, result);

    }

    @Test
    void should_save_game_launch_plan_in_launch_context() {
        //given
        GameLaunchPlanBuilder gameLaunchPlanBuilder = OperationFactoryFixture.gameLaunchBuilder;
        BuildGameLaunchPlanTask task = new BuildGameLaunchPlanTask(gameLaunchPlanBuilder);
        Manifest manifest = manifestService.loadManifest();
        context.setManifest(manifest);

        //when
        task.execute(context);

        //then
        assertNotNull(context.getGameLaunchPlan());

    }

}
