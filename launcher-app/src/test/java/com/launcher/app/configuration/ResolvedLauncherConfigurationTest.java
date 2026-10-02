package com.launcher.app.configuration;

import com.launcher.core.configuration.LauncherConfiguration;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResolvedLauncherConfigurationTest {

    @Test
    void should_create_resolved_launcher_configuration() {
        //given
        LauncherConfiguration expectedConfiguration = configuration();
        ManifestSourceKind expectedSourceKind = ManifestSourceKind.LOCAL_CONFIG;

        //when
        ResolvedLauncherConfiguration result = new ResolvedLauncherConfiguration(
                expectedConfiguration,
                expectedSourceKind
        );

        //then
        assertSame(expectedConfiguration, result.configuration());
        assertEquals(expectedSourceKind, result.sourceKind());
    }

    @Test
    void should_reject_null_source_kind() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new ResolvedLauncherConfiguration(
                        configuration(), null
                )
        );

        assertEquals("sourceKind", exception.getMessage());
    }

    @Test
    void should_reject_null_configuration() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new ResolvedLauncherConfiguration(
                        null, ManifestSourceKind.LOCAL_CONFIG
                )
        );

        assertEquals("configuration", exception.getMessage());
    }

    private LauncherConfiguration configuration() {
        return new LauncherConfiguration(
                URI.create("https://example.org/manifest.json"),
                Path.of("game-directory")
        );
    }
}
