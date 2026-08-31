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
import com.launcher.model.manifest.LoaderInfo;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.ManifestLoadResult;
import com.launcher.model.manifest.RuntimeLibrarySelection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildGameLaunchPlanTaskTest {
    private LaunchContext context;
    private final ManifestService manifestService = new RecordingManifestService();

    @BeforeEach
    void setUp() {
        context = OperationFactoryFixture.getContext();
    }

    @Test
    void should_return_failure_when_launch_info_is_missing() {
        //given
        GameLaunchPlanBuilder gameLaunchPlanBuilder = OperationFactoryFixture.gameLaunchPlanBuilder();
        BuildGameLaunchPlanTask task = new BuildGameLaunchPlanTask(gameLaunchPlanBuilder);
        context.setManifest(
                new Manifest(
                        "1.12.2",
                        new LoaderInfo("fabric", "0.16.10"),
                        List.of(),
                        null,
                        List.of()
                )
        );

        //when
        Result result = task.execute(context);

        //then
        assertInstanceOf(FailureResult.class, result);
        assertTrue(((FailureResult) result).getMessage().contains("Launch info not available"));
    }

    @Test
    void should_return_failure_when_runtime_library_selection_not_available() {
        //given
        GameLaunchPlanBuilder gameLaunchPlanBuilder = OperationFactoryFixture.gameLaunchPlanBuilder();
        BuildGameLaunchPlanTask task = new BuildGameLaunchPlanTask(gameLaunchPlanBuilder);

        ManifestLoadResult manifestLoadResult = manifestService.loadManifest();
        Manifest manifest = manifestLoadResult.manifest();

        context.setManifest(manifest);

        //when
        Result result = task.execute(context);

        //then
        assertInstanceOf(FailureResult.class, result);
        assertTrue(((FailureResult) result).getMessage().contains("Runtime library selection not available"));
    }

    @Test
    void should_return_failure_when_manifest_not_loaded() {
        //given
        GameLaunchPlanBuilder gameLaunchPlanBuilder = OperationFactoryFixture.gameLaunchPlanBuilder();
        BuildGameLaunchPlanTask task = new BuildGameLaunchPlanTask(gameLaunchPlanBuilder);

        //when
        Result result = task.execute(context);

        //then
        assertInstanceOf(FailureResult.class, result);
        assertTrue(((FailureResult) result).getMessage().contains("Manifest not loaded"));
    }

    @Test
    void should_return_build_game_launch_plan_state() {
        //given
        GameLaunchPlanBuilder gameLaunchPlanBuilder = OperationFactoryFixture.gameLaunchPlanBuilder();
        BuildGameLaunchPlanTask task = new BuildGameLaunchPlanTask(gameLaunchPlanBuilder);

        //then
        assertEquals(LauncherState.BUILDING_GAME_LAUNCH_PLAN, task.state());
    }

    @Test
    void should_return_success_result_when_game_launch_plan_is_built() {
        //given
        GameLaunchPlanBuilder gameLaunchPlanBuilder = OperationFactoryFixture.gameLaunchPlanBuilder();
        BuildGameLaunchPlanTask task = new BuildGameLaunchPlanTask(gameLaunchPlanBuilder);
        ManifestLoadResult manifestLoadResult = manifestService.loadManifest();
        Manifest manifest = manifestLoadResult.manifest();
        RuntimeLibrarySelection runtimeLibrarySelection = manifestLoadResult.runtimeLibrarySelection();

        context.setManifest(manifest);
        context.setRuntimeLibrarySelection(runtimeLibrarySelection);

        //when
        Result result = task.execute(context);

        //then
        assertInstanceOf(SuccessResult.class, result);

    }

    @Test
    void should_save_game_launch_plan_in_launch_context() {
        //given
        GameLaunchPlanBuilder gameLaunchPlanBuilder = OperationFactoryFixture.gameLaunchPlanBuilder();
        BuildGameLaunchPlanTask task = new BuildGameLaunchPlanTask(gameLaunchPlanBuilder);
        ManifestLoadResult manifestLoadResult = manifestService.loadManifest();
        Manifest manifest = manifestLoadResult.manifest();
        RuntimeLibrarySelection runtimeLibrarySelection = manifestLoadResult.runtimeLibrarySelection();

        context.setManifest(manifest);
        context.setRuntimeLibrarySelection(runtimeLibrarySelection);

        //when
        task.execute(context);

        //then
        assertNotNull(context.getGameLaunchPlan());

    }

}
