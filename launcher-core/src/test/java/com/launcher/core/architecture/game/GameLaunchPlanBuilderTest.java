package com.launcher.core.architecture.game;

import com.launcher.core.architecture.support.recording.*;
import com.launcher.core.game.GameLaunchPlan;
import com.launcher.core.game.GameLaunchPlanBuilder;
import com.launcher.model.manifest.Manifest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameLaunchPlanBuilderTest {

    @Test
    void should_build_game_launch_plan_with_game_directory_from_directory_provider() {
        //given
        RecordingDirectoryProvider directoryProvider = new RecordingDirectoryProvider();
        RecordingDefaultGameLaunchCommandBuilder launchCommandBuilder = new RecordingDefaultGameLaunchCommandBuilder();
        RecordingGameClasspathBuilder recordingGameClasspathBuilder = new RecordingGameClasspathBuilder();
        RecordingClasspathFormatter recordingClasspathFormatter = new RecordingClasspathFormatter();

        GameLaunchPlanBuilder gameLaunchPlanBuilder = new GameLaunchPlanBuilder(
                directoryProvider,
                launchCommandBuilder,
                recordingGameClasspathBuilder,
                recordingClasspathFormatter
        );

        Manifest manifest = new RecordingManifestService().loadManifest();

        //when
        GameLaunchPlan gameLaunchPlan = gameLaunchPlanBuilder.build(manifest);

        //then
        assertEquals(
                directoryProvider.directories().game(),
                gameLaunchPlan.gameDirectory()
        );

        assertEquals(
                List.of("test-command"),
                gameLaunchPlan.command()
        );

        assertEquals(
                manifest.launchInfo(),
                launchCommandBuilder.getLaunchInfo()
        );

        assertEquals(
                manifest.minecraftVersion(),
                launchCommandBuilder.getLaunchVariables().versionName()
        );

        assertEquals(
                directoryProvider.directories().game(),
                launchCommandBuilder.getLaunchVariables().gameDirectory()
        );

        assertEquals(
                "path.to.not.BlankValue",
                launchCommandBuilder.getLaunchVariables().classpath()
        );

        assertEquals(
                manifest,
                recordingGameClasspathBuilder.getManifest()
        );

        assertEquals(
                directoryProvider.directories().game(),
                recordingGameClasspathBuilder.getGameDirectory()
        );

        assertEquals(
                recordingGameClasspathBuilder.getGameClasspath(),
                recordingClasspathFormatter.getGameClasspath()
        );
    }

}
