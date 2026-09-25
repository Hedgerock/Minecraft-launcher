package com.launcher.app.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PropertiesManifestUriSourceTest {

    @TempDir
    Path tempDirectory;

    private final PropertiesManifestUriSource source = new PropertiesManifestUriSource();

    @Test
    void should_reject_uri_without_http_or_https() throws IOException {
        //given
        Path configurationFile = tempDirectory.resolve("launcherPropertiesWithCorruptedManifestUri");

        Files.writeString(
                configurationFile,
                "manifest.uri=ftp://example.org/manifest.json",
                StandardCharsets.UTF_8
        );

        //when & then
        assertThrows(
                ManifestUriConfigurationException.class,
                () -> source.load(configurationFile)
        );
    }

    @Test
    void should_reject_uri_without_host() throws IOException {
        //given
        Path configurationFile = tempDirectory.resolve("launcherPropertiesWithCorruptedManifestUri");

        Files.writeString(
                configurationFile,
                "manifest.uri=https:3000/manifest.jar",
                StandardCharsets.UTF_8
        );

        //when & then
        assertThrows(
                ManifestUriConfigurationException.class,
                () -> source.load(configurationFile)
        );
    }

    @Test
    void should_reject_invalid_unicode_escape_in_configuration_file() throws IOException {
        //given
        Path configurationFile = tempDirectory.resolve("launcherPropertiesWithInvalidManifestUri");

        Files.writeString(
                configurationFile,
                "manifest.uri=https://example.org/\\uZZZZ/manifest.jar",
                StandardCharsets.UTF_8
        );

        //when & then
        assertThrows(
                ManifestUriConfigurationException.class,
                () -> source.load(configurationFile)
        );
    }

    @Test
    void should_reject_invalid_uri() throws IOException {
        //given
        Path configurationFile = tempDirectory.resolve("launcherPropertiesWithInvalidManifestUri");

        Files.writeString(
                configurationFile,
                "manifest.uri=https://example.org/invalid[uri"
        );

        //when & then
        assertThrows(
                ManifestUriConfigurationException.class,
                () -> source.load(configurationFile)
        );
    }

    @Test
    void should_reject_missing_manifest_uri_property() throws IOException {
        //given
        Path configurationFile = tempDirectory.resolve("launcherPropertiesWithoutManifestUri");

        Files.writeString(
                configurationFile,
                "manifest.name=testManifest",
                StandardCharsets.UTF_8
        );

        //when & then
        assertThrows(
                ManifestUriConfigurationException.class,
                () -> source.load(configurationFile)
        );
    }

    @Test
    void should_reject_blank_manifest_uri_property() throws IOException {
        //given
        Path configurationFile = tempDirectory.resolve("launcherPropertiesWithBlankManifestUri");

        Files.writeString(
                configurationFile,
                "manifest.uri= ",
                StandardCharsets.UTF_8
        );

        //when & then
        assertThrows(
                ManifestUriConfigurationException.class,
                () -> source.load(configurationFile)
        );
    }

    @Test
    void should_reject_null_configuration_file() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> source.load(null)
        );

        assertEquals("configurationFile", exception.getMessage());
    }

    @Test
    void should_reject_missing_configuration_file() {
        //given
        Path configurationFile = tempDirectory.resolve("missing.properties");

        //when & then
        assertThrows(
                ManifestUriConfigurationException.class,
                () -> source.load(configurationFile)
        );
    }

    @Test
    void should_load_manifest_uri_from_configuration_file_with_http_manifest_uri_property()
            throws IOException {
        //given
        Path configurationFile = tempDirectory.resolve("launcher.properties");

        Files.writeString(
                configurationFile,
                "manifest.uri=http://example.org/manifest.json",
                StandardCharsets.UTF_8
        );

        //when
        URI result = source.load(configurationFile);

        //then
        assertEquals(
                URI.create("http://example.org/manifest.json"),
                result
        );
    }

    @Test
    void should_load_manifest_uri_from_configuration_file_with_https_manifest_uri_property()
            throws IOException {
        //given
        Path configurationFile = tempDirectory.resolve("launcher.properties");

        Files.writeString(
                configurationFile,
                "manifest.uri=https://example.org/manifest.json",
                StandardCharsets.UTF_8
        );

        //when
        URI result = source.load(configurationFile);

        //then
        assertEquals(
                URI.create("https://example.org/manifest.json"),
                result
        );
    }
}
