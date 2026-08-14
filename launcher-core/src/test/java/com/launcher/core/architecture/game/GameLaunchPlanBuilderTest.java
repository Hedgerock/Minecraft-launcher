package com.launcher.core.architecture.game;

import com.launcher.core.architecture.support.recording.RecordingDirectoryProvider;
import com.launcher.core.architecture.support.recording.RecordingManifestService;
import com.launcher.core.game.GameLaunchPlan;
import com.launcher.core.game.GameLaunchPlanBuilder;
import com.launcher.core.manifest.ManifestService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameLaunchPlanBuilderTest {

    @Test
    void should_build_game_launch_plan_with_game_directory_from_directory_provider() {
        //given
        RecordingDirectoryProvider directoryProvider = new RecordingDirectoryProvider();
        GameLaunchPlanBuilder gameLaunchPlanBuilder = new GameLaunchPlanBuilder(directoryProvider);
        ManifestService manifestService = new RecordingManifestService();

        //when
        GameLaunchPlan gameLaunchPlan = gameLaunchPlanBuilder.build(manifestService.loadManifest());

        //then
        assertEquals(
                directoryProvider.directories().game(),
                gameLaunchPlan.gameDirectory()
        );

        assertEquals(
                List.of(
                        "java",
                        "first-jvm-argument",
                        "second-jvm-argument",
                        "TestMain",
                        "first-game-argument",
                        "second-game-argument"
                ),
                gameLaunchPlan.command()
        );

    }

}
