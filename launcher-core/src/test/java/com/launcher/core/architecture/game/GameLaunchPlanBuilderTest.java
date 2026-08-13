package com.launcher.core.architecture.game;

import com.launcher.core.architecture.support.recording.RecordingDirectoryProvider;
import com.launcher.core.game.GameLaunchPlan;
import com.launcher.core.game.GameLaunchPlanBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameLaunchPlanBuilderTest {

    @Test
    void should_build_game_launch_plan_with_game_directory_from_directory_provider() {
        //given
        RecordingDirectoryProvider directoryProvider = new RecordingDirectoryProvider();
        GameLaunchPlanBuilder gameLaunchPlanBuilder = new GameLaunchPlanBuilder(directoryProvider);

        //when
        GameLaunchPlan gameLaunchPlan = gameLaunchPlanBuilder.build();

        //then
        assertEquals(
                directoryProvider.directories().game(),
                gameLaunchPlan.gameDirectory()
        );

    }

}
