package com.launcher.core.architecture.game;

import com.launcher.core.architecture.support.recording.RecordingGameService;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.game.GameLaunchPlan;
import com.launcher.core.game.LaunchGameTask;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.result.FailureResult;
import com.launcher.core.result.Result;
import com.launcher.core.result.SuccessResult;
import com.launcher.core.state.LauncherState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LaunchGameTaskTest {
    private RecordingGameService recordingGameService;
    private LaunchGameTask gameTask;
    private LaunchContext context;

    private LaunchContext getContext() {
        return new LaunchContext(
                new LauncherConfiguration(
                        URI.create("currentPath"),
                        Path.of("")
                )
        );
    }

    @BeforeEach
    void setUp() {
        recordingGameService = new RecordingGameService(
                new GameLaunchPlan(
                        Path.of("game")
                )
        );
        gameTask = new LaunchGameTask(recordingGameService);
        context = getContext();
    }

    @Test
    void should_transfer_game_launcher_plan_to_game_service() {
        //given
        context.setGameLaunchPlan(recordingGameService.getReceivedGameLaunchPlan());

        //when
        gameTask.execute(context);

        //then
        GameLaunchPlan expectedPlan = new GameLaunchPlan(
            Path.of("game")
        );

        assertEquals(
                expectedPlan,
                recordingGameService.getReceivedGameLaunchPlan()
        );
    }

    @Test
    void should_return_failure_when_game_launch_plan_is_missing() {

        //when
        Result result = gameTask.execute(context);

        //then
        assertFalse(result.success());
        assertInstanceOf(FailureResult.class, result);

    }

    @Test
    void should_return_launching_state() {
        //when
        gameTask.execute(context);

        //then
        assertEquals(
                LauncherState.LAUNCHING,
                gameTask.state()
        );
    }

    @Test
    void should_return_success_when_game_is_launched() {
        context.setGameLaunchPlan(recordingGameService.getReceivedGameLaunchPlan());

        //when
        Result result = gameTask.execute(context);

        //then
        assertInstanceOf(SuccessResult.class, result);

    }

    @Test
    void should_launch_game() {
        context.setGameLaunchPlan(recordingGameService.getReceivedGameLaunchPlan());

        //when
        gameTask.execute(context);

        //then
        assertTrue(recordingGameService.isLaunchCalled());

    }

}
