package com.launcher.game.service;

import com.launcher.core.game.GameLaunchPlan;
import com.launcher.game.exception.GameLaunchException;
import com.launcher.game.support.RecordingGameProcessLauncher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultGameServiceTest {
    private GameLaunchPlan plan;

    @BeforeEach
    void setUp() {
        plan = new GameLaunchPlan(
                Path.of("game"),
                List.of("java", "TestMain")
        );
    }

    @Test
    void should_propagate_game_launch_exception() {
        RecordingGameProcessLauncher launcher = new RecordingGameProcessLauncher(true);
        DefaultGameService launcherService = new DefaultGameService(launcher);

        //when & then
        assertThrows(
                GameLaunchException.class,
                () -> launcherService.launch(plan)
        );

    }

    @Test
    void should_pass_game_launch_plan_to_game_process_launcher() {
        //given
        RecordingGameProcessLauncher launcher = new RecordingGameProcessLauncher();
        DefaultGameService service = new DefaultGameService(launcher);

        //when
        service.launch(plan);

        //then
        assertEquals(launcher.getPlan(), plan);

    }

}
