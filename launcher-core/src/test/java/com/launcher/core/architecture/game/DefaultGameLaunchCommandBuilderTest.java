package com.launcher.core.architecture.game;

import com.launcher.core.architecture.support.recording.RecordingManifestService;
import com.launcher.core.game.builder.DefaultGameLaunchCommandBuilder;
import com.launcher.core.resolve.DefaultLaunchArgumentResolver;
import com.launcher.core.resolve.model.LaunchVariables;
import com.launcher.model.manifest.LaunchInfo;
import com.launcher.model.runtime.JavaExecutableReference;
import com.launcher.model.runtime.JavaVersionRequirement;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultGameLaunchCommandBuilderTest {

    @Test
    void should_build_command_when_auth_args_are_empty() {
        //given
        DefaultGameLaunchCommandBuilder builder = getCommandBuilder();

        LaunchInfo launchInfo = getLaunchInfoWithoutAuthArgs();
        LaunchVariables launchVariables = getLaunchVariables();

        //when
        List<String> result = builder.build(
                launchInfo,
                launchVariables,
                JavaExecutableReference.commandName("java-custom")
        );

        //then
        assertEquals(
                List.of(
                        "java-custom",
                        "-cp",
                        "classpath.to.CurrentClass",
                        "TestMain",
                        "-gameDir",
                        "test-directory"
                ),
                result
        );
    }

    @Test
    void should_resolve_supported_placeholders_in_auth_args() {
        //given
        DefaultGameLaunchCommandBuilder builder = getCommandBuilder();
        LaunchInfo launchInfo = getLaunchInfoWithAuthArgsPlaceholder();

        LaunchVariables launchVariables = getLaunchVariables();

        //when
        List<String> result = builder.build(
                launchInfo,
                launchVariables,
                JavaExecutableReference.commandName("java-custom")
        );

        //then
        assertEquals(
                List.of(
                        "java-custom",
                        "MainClass",
                        "-version",
                        "1.12.1"
                ),
                result
        );
    }

    @Test
    void should_build_command_from_launch_info() {
        //given
        DefaultGameLaunchCommandBuilder commandBuilder = getCommandBuilder();
        LaunchInfo launchInfo = getLaunchInfo();

        LaunchVariables launchVariables = getLaunchVariables();

        //when
        List<String> result = commandBuilder.build(
                launchInfo,
                launchVariables,
                JavaExecutableReference.commandName("java-custom")
        );

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
                        "test-directory",
                        "--accessToken",
                        "${access_token}"
                ),
                result
        );

        assertEquals(
                "java-custom",
                result.getFirst()
        );

    }

    private LaunchInfo getLaunchInfo() {
        return new RecordingManifestService().loadManifest().manifest().launchInfo();
    }

    private LaunchVariables getLaunchVariables() {
        return new LaunchVariables(
                "1.12.1",
                Path.of("test-directory"),
                "classpath.to.CurrentClass",
                Path.of("natives-directory")
        );
    }

    private LaunchInfo getLaunchInfoWithoutAuthArgs() {
        return new LaunchInfo(
                "TestMain",
                List.of(
                        "-cp",
                        "${classpath}"
                ),
                List.of(
                        "-gameDir",
                        "${game_directory}"
                ),
                List.of(
                        "test-value.jar",
                        "test-value2.jar"
                ),
                "java-custom",
                new JavaVersionRequirement(17)
        );
    }

    private LaunchInfo getLaunchInfoWithAuthArgsPlaceholder() {
        return new LaunchInfo(
                "MainClass",
                List.of(),
                List.of(),
                List.of("run-command"),
                "java-custom",
                new JavaVersionRequirement(17),
                List.of("-version", "${version_name}")
        );
    }

    private DefaultGameLaunchCommandBuilder getCommandBuilder() {
        return new DefaultGameLaunchCommandBuilder(
                new DefaultLaunchArgumentResolver()
        );
    }

}
