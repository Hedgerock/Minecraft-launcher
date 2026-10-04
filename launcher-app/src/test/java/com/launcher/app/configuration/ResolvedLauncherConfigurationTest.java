package com.launcher.app.configuration;

import com.launcher.core.configuration.LauncherConfiguration;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResolvedLauncherConfigurationTest {

    @Test
    void should_reject_when_launcher_configuration_and_managed_uris_has_different_manifest_uri() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ResolvedLauncherConfiguration(
                        configuration(), ManifestSourceKind.MANAGED,
                        Optional.of(
                                new ManagedManifestUris(
                                        URI.create("https://example.com/manifest.json"),
                                        URI.create("https://example.com/manifest.sig")
                                )
                        )
                )
        );

        assertEquals("manifestUri and managedManifestUri must be identical", exception.getMessage());
    }

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
    void should_reject_when_source_kind_is_not_managed_but_has_manifest_managed_uris() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ResolvedLauncherConfiguration(
                        configuration(), ManifestSourceKind.LOCAL_CONFIG,
                        Optional.of(
                                new ManagedManifestUris(
                                        configuration().manifestUri(),
                                        URI.create("https://example.com/manifest.sig")
                                )
                        )
                )
        );

        assertEquals("managedManifestUris must be empty", exception.getMessage());
    }

    @Test
    void should_reject_when_source_kind_is_managed_and_managed_manifest_uris_are_empty() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ResolvedLauncherConfiguration(
                        configuration(), ManifestSourceKind.MANAGED, Optional.empty()
                )
        );

        assertEquals("managedManifestUris must not be empty", exception.getMessage());
    }

    @Test
    void should_reject_null_managed_manifest_uris() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new ResolvedLauncherConfiguration(
                        configuration(), ManifestSourceKind.LOCAL_CONFIG, null
                )
        );

        assertEquals("managedManifestUris", exception.getMessage());
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
