package com.launcher.core.architecture.game.classpath.builder;

import com.launcher.core.game.classpath.GameClasspath;
import com.launcher.core.game.classpath.builder.DefaultGameClasspathBuilder;
import com.launcher.model.manifest.LaunchInfo;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultGameClasspathBuilderTest {
    private final DefaultGameClasspathBuilder builder = new DefaultGameClasspathBuilder();

    @Test
    void should_reject_null_launch_info() {
        //given
        Path gameDirectory = Path.of("game-directory");

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> builder.build(null, gameDirectory)
        );

        assertTrue(exception.getMessage().contains("launchInfo"));
    }

    @Test
    void should_reject_null_game_directory() {
        //given
        LaunchInfo launchInfo = new LaunchInfo(
                "MainClass",
                List.of("-cp", "${classpath}"),
                List.of("--username", "Player"),
                List.of("libraries/example.jar", "client.jar")
        );

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> builder.build(launchInfo, null)
        );

        assertTrue(exception.getMessage().contains("gameDirectory"));
    }

    @Test
    void should_build_game_classpath_from_launch_info_classpath_entry() {
        //given
        LaunchInfo launchInfo = new LaunchInfo(
                "MainClass",
                List.of("-cp", "${classpath}"),
                List.of("--username", "Player"),
                List.of("libraries/example.jar", "client.jar")
        );

        Path gameDirectory = Path.of("game-directory");

        //when
        GameClasspath classpath = builder.build(launchInfo, gameDirectory);

        //then
        assertEquals(
                List.of(
                        gameDirectory.resolve("libraries/example.jar"),
                        gameDirectory.resolve("client.jar")
                ),
                classpath.entries()
        );
    }

}
