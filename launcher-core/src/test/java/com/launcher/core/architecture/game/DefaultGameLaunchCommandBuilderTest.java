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

    private LaunchInfo getLaunchInfo() {
        return new RecordingManifestService().loadManifest().manifest().launchInfo();
    }

    @Test
    void should_build_command_from_launch_info() {
        //given
        DefaultGameLaunchCommandBuilder commandBuilder = new DefaultGameLaunchCommandBuilder(
                new DefaultLaunchArgumentResolver()
        );
        LaunchInfo launchInfo = getLaunchInfo();

        LaunchVariables launchVariables = new LaunchVariables(
                "1.12.1",
                Path.of("test-directory"),
                "classpath.to.CurrentClass"
        );

        //when
        List<String> command = commandBuilder.build(launchInfo, launchVariables);

        //then
        assertEquals(
                List.of(
                        "java-custom",
                        "first-jvm-argument",
                        "second-jvm-argument",
                        "-cp",
                        "classpath.to.CurrentClass",
                        "TestMain",
                        "first-game-argument",
                        "second-game-argument",
                        "-gameDir",
                        "test-directory"
                ),
                command
        );

        assertEquals(
                "java-custom",
                command.getFirst()
        );

    }

}
