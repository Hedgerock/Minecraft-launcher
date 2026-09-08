package com.launcher.core.architecture.configuration;

import com.launcher.core.configuration.LauncherConfiguration;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LauncherConfigurationTest {
    private static final URI DEFAULT_URI = URI.create("https://example.com/manifest.json");
    private static final Path DEFAULT_LAUNCHER_DIRECTORY = Path.of("/tmp");
    private static final String DEFAULT_JAVA_EXECUTABLE_OVERRIDE = "DefaultValue";

    @Test
    void should_create_configuration_with_java_executable_override() {
        //given & when
        LauncherConfiguration configuration = new LauncherConfiguration(
                DEFAULT_URI,
                DEFAULT_LAUNCHER_DIRECTORY,
                Optional.of(DEFAULT_JAVA_EXECUTABLE_OVERRIDE)
        );

        //then
        assertEquals(
                Optional.of(DEFAULT_JAVA_EXECUTABLE_OVERRIDE),
                configuration.javaExecutableOverride()
        );
    }

    @Test
    void should_create_configuration_without_java_executable_override() {
        //given & when
        LauncherConfiguration configuration = new LauncherConfiguration(
                DEFAULT_URI,
                DEFAULT_LAUNCHER_DIRECTORY
        );

        //then
        assertEquals(
                Optional.empty(),
                configuration.javaExecutableOverride()
        );
    }

    @Test
    void should_reject_null_java_executable_override() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LauncherConfiguration(
                        DEFAULT_URI,
                        DEFAULT_LAUNCHER_DIRECTORY,
                        null
                )
        );

        assertEquals(
                "javaExecutableOverride",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_launcher_directory() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LauncherConfiguration(
                        DEFAULT_URI,
                        null,
                        Optional.of(DEFAULT_JAVA_EXECUTABLE_OVERRIDE)
                )
        );

        assertEquals(
                "launcherDirectory",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_manifest_uri() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LauncherConfiguration(
                        null,
                        DEFAULT_LAUNCHER_DIRECTORY,
                        Optional.of(DEFAULT_JAVA_EXECUTABLE_OVERRIDE)
                )
        );

        assertEquals(
                "manifestUri",
                exception.getMessage()
        );
    }
}
