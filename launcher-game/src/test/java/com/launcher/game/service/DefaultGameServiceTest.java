package com.launcher.game.service;

import com.launcher.core.game.GameLaunchPlan;
import com.launcher.core.game.GameService;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DefaultGameServiceTest {

    @Test
    void should_launch_without_error_for_current_scaffold() {
        //given
        GameService service = new DefaultGameService();
        GameLaunchPlan gameLaunchPlan = new GameLaunchPlan(
                Path.of("current-game-path")
        );

        //then
        assertDoesNotThrow(() -> service.launch(gameLaunchPlan));
        assertInstanceOf(DefaultGameService.class, service);

    }

}
