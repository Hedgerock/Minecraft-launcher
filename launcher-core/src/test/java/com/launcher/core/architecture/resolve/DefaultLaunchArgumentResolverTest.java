package com.launcher.core.architecture.resolve;

import com.launcher.core.resolve.DefaultLaunchArgumentResolver;
import com.launcher.core.resolve.model.LaunchVariables;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultLaunchArgumentResolverTest {
    private final DefaultLaunchArgumentResolver underTest = new DefaultLaunchArgumentResolver();

    @Test
    void should_resolve_assets_index_name_placeholder() {
        //given
        List<String> arguments = List.of(
                "-assetsRoot=${assets_root}",
                "-assetIndexName=${assets_index_name}"
        );
        Path gameDirectory = Path.of("game-path");
        Path nativesDirectory = gameDirectory.resolve("natives");

        LaunchVariables launchVariables = new LaunchVariables(
                "1.12.1",
                gameDirectory,
                "net.minecraft.launchwrapper.Launch",
                nativesDirectory,
                Path.of("assets-root"),
                "assetsIndexNameValue"
        );

        //when
        List<String> result = underTest.resolve(arguments, launchVariables);

        //then
        assertEquals(
                "-assetIndexName=assetsIndexNameValue",
                result.getLast()
        );
    }

    @Test
    void should_throw_when_arguments_contains_assets_index_name_placeholder_without_assets_index_name() {
        //given
        List<String> arguments = List.of(
                "-assetsRoot=${assets_root}",
                "-assetIndexName=${assets_index_name}"
        );
        Path gameDirectory = Path.of("game-path");
        Path nativesDirectory = gameDirectory.resolve("natives");

        LaunchVariables launchVariables = new LaunchVariables(
                "1.12.1",
                gameDirectory,
                "net.minecraft.launchwrapper.Launch",
                nativesDirectory,
                Path.of("assets-root")
        );

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> underTest.resolve(arguments, launchVariables)
        );
    }

    @Test
    void should_resolve_assets_root_placeholder() {
        //given
        List<String> arguments = List.of(
                "-assetsRoot=${assets_root}"
        );
        Path gameDirectory = Path.of("game-path");
        Path nativesDirectory = gameDirectory.resolve("natives");

        LaunchVariables launchVariables = new LaunchVariables(
                "1.12.1",
                gameDirectory,
                "net.minecraft.launchwrapper.Launch",
                nativesDirectory,
                Path.of("assets-root")
        );

        //when
        List<String> result = underTest.resolve(arguments, launchVariables);

        //then
        assertEquals(
                "-assetsRoot=assets-root",
                result.getFirst()
        );
    }

    @Test
    void should_resolve_natives_directory_placeholder() {
        //given
        List<String> arguments = List.of(
                "-Djava.library.path=${natives_directory}"
        );
        Path gameDirectory = Path.of("game-path");
        Path nativesDirectory = gameDirectory.resolve("natives");

        LaunchVariables launchVariables = new LaunchVariables(
                "1.12.1",
                gameDirectory,
                "net.minecraft.launchwrapper.Launch",
                nativesDirectory,
                Path.of("assets-root")
        );

        //when
        List<String> result = underTest.resolve(arguments, launchVariables);

        //then
        assertEquals(
                "-Djava.library.path=" + nativesDirectory,
                result.getFirst()
        );
    }

    @Test
    void should_resolve_classpath_placeholder() {
        //given
        List<String> arguments = getArguments();

        LaunchVariables launchVariables = new LaunchVariables(
                "1.12.1",
                Path.of("game-path"),
                "net.minecraft.launchwrapper.Launch",
                Path.of("natives-directory"),
                Path.of("assets-root")
        );

        //when
        List<String> result = underTest.resolve(arguments, launchVariables);

        //then
        assertEquals("net.minecraft.launchwrapper.Launch", result.get(7));
    }

    @Test
    void should_reject_null_value_in_the_list_of_arguments() {
        //given
        List<String> arguments = new ArrayList<>();
        arguments.add("--version");
        arguments.add(null);

        LaunchVariables launchVariables = new LaunchVariables(
                "1.12.1",
                Path.of("game-path"),
                "net.minecraft.launchwrapper.Launch",
                Path.of("natives-directory"),
                Path.of("assets-root")
        );

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> underTest.resolve(arguments, launchVariables)
        );
    }

    @Test
    void should_reject_null_launch_variables() {
        //given
        List<String> arguments = getArguments();

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> underTest.resolve(arguments, null)
        );

        assertTrue(exception.getMessage().contains("variables"));
    }

    @Test
    void should_reject_null_arguments() {
        //given
        LaunchVariables launchVariables = new LaunchVariables(
                "1.12.1",
                Path.of("game-path"),
                "net.minecraft.launchwrapper.Launch",
                Path.of("natives-directory"),
                Path.of("assets-root")
        );

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> underTest.resolve(null, launchVariables)
        );

        assertTrue(exception.getMessage().contains("arguments"));
    }

    @Test
    void should_keep_unknown_placeholder_unchanged() {
        //given
        List<String> arguments = getArguments();

        LaunchVariables launchVariables = new LaunchVariables(
                "1.12.1",
                Path.of("game-path"),
                "net.minecraft.launchwrapper.Launch",
                Path.of("natives-directory"),
                Path.of("assets-root")
        );

        //when
        List<String> result = underTest.resolve(arguments, launchVariables);

        //then
        assertEquals("1.12.1", result.get(1));
        assertEquals("game-path", result.get(3));
        assertEquals("${unknown_placeholder}", result.get(5));
    }

    @Test
    void should_resolve_game_dir_placeholder() {
        //given
        List<String> arguments = getArguments();

        LaunchVariables launchVariables = new LaunchVariables(
                "1.12.1",
                Path.of("game-path"),
                "net.minecraft.launchwrapper.Launch",
                Path.of("natives-directory"),
                Path.of("assets-root")
        );

        //when
        List<String> result = underTest.resolve(arguments, launchVariables);

        //then
        assertTrue(result.get(3).contains("game-path"));
    }

    @Test
    void should_resolve_version_name_placeholder() {
        //given
        List<String> arguments = getArguments();

        LaunchVariables launchVariables = new LaunchVariables(
                "1.12.1",
                Path.of("game-path"),
                "net.minecraft.launchwrapper.Launch",
                Path.of("natives-directory"),
                Path.of("assets-root")
        );

        //when
        List<String> result = underTest.resolve(arguments, launchVariables);

        //then
        assertTrue(result.get(1).contains("1.12.1"));

    }

    private List<String> getArguments() {
        return List.of(
                "--version",
                "${version_name}",
                "--gameDir",
                "${game_directory}",
                "--unknown",
                "${unknown_placeholder}",
                "--cp",
                "${classpath}",
                "--nativesDir",
                "${natives_directory}",
                "${assets_root}"
        );
    }
}
