package com.launcher.core.architecture.game;

import com.launcher.core.architecture.support.recording.RecordingManifestService;
import com.launcher.core.game.builder.DefaultGameLaunchCommandBuilder;
import com.launcher.core.resolve.DefaultLaunchArgumentResolver;
import com.launcher.core.resolve.model.LaunchVariables;
import com.launcher.model.manifest.LaunchInfo;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultGameLaunchCommandBuilderTest {

    @Test
    void should_build_command_from_launch_info() {
        //given
        LaunchInfo launchInfo = new RecordingManifestService().loadManifest().launchInfo();

        DefaultGameLaunchCommandBuilder commandBuilder = new DefaultGameLaunchCommandBuilder(
                new DefaultLaunchArgumentResolver()
        );

        LaunchVariables launchVariables = new LaunchVariables(
                "1.12.1",
                Path.of("test-directory")
        );

        //when
        List<String> command = commandBuilder.build(launchInfo, launchVariables);

        //then
        assertEquals(
                List.of(
                        "java",
                        "first-jvm-argument",
                        "second-jvm-argument",
                        "TestMain",
                        "first-game-argument",
                        "second-game-argument"
                ),
                command
        );

    }

}
