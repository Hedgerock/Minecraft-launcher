package com.launcher.core.architecture.resolve.model;

import com.launcher.core.resolve.model.LaunchVariables;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LaunchVariablesTest {

    @Test
    void should_reject_null_game_directory() {
        //given
        String versionName = "test";

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LaunchVariables(versionName, null)
        );

        assertTrue(exception.getMessage().contains("gameDirectory"));
    }

    @Test
    void should_reject_null_version_name() {
        //given
        Path gameDirectory = Path.of("game-directory");

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LaunchVariables(null, gameDirectory)
        );

        assertTrue(exception.getMessage().contains("versionName"));
    }

    @Test
    void should_reject_blank_version_name() {
        //given
        Path gameDirectory = Path.of("game-directory");
        String versionName = " ";

        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LaunchVariables(versionName, gameDirectory)
        );

        assertTrue(exception.getMessage().contains("versionName must not be blank"));
    }

}
