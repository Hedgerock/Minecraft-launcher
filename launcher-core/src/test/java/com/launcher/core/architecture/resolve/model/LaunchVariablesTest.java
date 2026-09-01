package com.launcher.core.architecture.resolve.model;

import com.launcher.core.resolve.model.LaunchVariables;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LaunchVariablesTest {

    @Test
    void should_reject_null_natives_directory() {
        //given
        String versionName = "test";
        Path gameDirectory = Path.of("test-directory");
        String classpath = "test.classpath.TestClass";

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LaunchVariables(
                        versionName,
                        gameDirectory,
                        classpath,
                        null
                )
        );

        assertTrue(exception.getMessage().contains("nativesDirectory"));
    }

    @Test
    void should_reject_blank_classpath() {
        //given
        String versionName = "test";
        Path gameDirectory = Path.of("test-directory");
        String classpath = " ";
        Path nativesDirectory = Path.of("natives-directory");

        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LaunchVariables(
                        versionName,
                        gameDirectory,
                        classpath,
                        nativesDirectory
                )
        );

        assertTrue(exception.getMessage().contains("classpath must not be blank"));
    }

    @Test
    void should_reject_null_classpath() {
        //given
        String versionName = "test";
        Path gameDirectory = Path.of("test-directory");
        Path nativesDirectory = Path.of("natives-directory");

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LaunchVariables(
                        versionName,
                        gameDirectory,
                        null,
                        nativesDirectory
                )
        );

        assertTrue(exception.getMessage().contains("classpath"));
    }

    @Test
    void should_reject_null_game_directory() {
        //given
        String versionName = "test";
        String classpath = "test.classpath.TestClass";
        Path nativesDirectory = Path.of("natives-directory");

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LaunchVariables(
                        versionName,
                        null,
                        classpath,
                        nativesDirectory
                )
        );

        assertTrue(exception.getMessage().contains("gameDirectory"));
    }

    @Test
    void should_reject_null_version_name() {
        //given
        Path gameDirectory = Path.of("game-directory");
        String classpath = "test.classpath.TestClass";

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LaunchVariables(
                        null,
                        gameDirectory,
                        classpath,
                        Path.of("natives-directory")
                )
        );

        assertTrue(exception.getMessage().contains("versionName"));
    }

    @Test
    void should_reject_blank_version_name() {
        //given
        Path gameDirectory = Path.of("game-directory");
        String versionName = " ";
        String classpath = "test.classpath.TestClass";
        Path nativesDirectory = Path.of("natives-directory");

        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LaunchVariables(
                        versionName,
                        gameDirectory,
                        classpath,
                        nativesDirectory
                )
        );

        assertTrue(exception.getMessage().contains("versionName must not be blank"));
    }

}
