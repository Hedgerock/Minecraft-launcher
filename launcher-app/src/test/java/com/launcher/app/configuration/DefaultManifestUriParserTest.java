package com.launcher.app.configuration;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultManifestUriParserTest {
    private final DefaultManifestUriParser parser = new DefaultManifestUriParser();

    @Test
    void should_create_uri_for_managed_source_kind() {
        //given
        String candidate = "https://example.org/manifest.json";

        //when
        URI result = parser.parseManaged(candidate);

        //then
        assertEquals(
                URI.create(candidate),
                result
        );
    }

    @Test
    void should_reject_non_https_for_managed_uri() {
        //given
        String candidate = "http://localhost:3000/manifest.json";

        //when & then
        assertThrows(
                ManifestUriConfigurationException.class,
                () -> parser.parseManaged(candidate)
        );
    }

    @Test
    void should_create_uri_with_https_prefix() {
        //given
        String candidate = "https://example.org/manifest.json";

        //when
        URI result = parser.parse(candidate);

        //then
        assertEquals(
                URI.create(candidate),
                result
        );
    }

    @Test
    void should_create_uri_with_http_prefix() {
        //given
        String candidate = "http://localhost:3000/manifest.json";

        //when
        URI result = parser.parse(candidate);

        //then
        assertEquals(
                URI.create(candidate),
                result
        );
    }

    @Test
    void should_throw_for_non_http_https_uri() {
        //when & then
        assertThrows(
                ManifestUriConfigurationException.class,
                () -> parser.parse("ftp://example.org/manifest.jar")
        );
    }

    @Test
    void should_throw_without_host() {
        //when & then
        assertThrows(
                ManifestUriConfigurationException.class,
                () -> parser.parse("https:3000/manifest.jar")
        );
    }

    @Test
    void should_throw_when_manifest_uri_is_invalid() {
        //when & then
        ManifestUriConfigurationException exception = assertThrows(
                ManifestUriConfigurationException.class,
                () -> parser.parse("https://example.org/invalid[uri")
        );

        assertInstanceOf(
                IllegalArgumentException.class,
                exception.getCause()
        );
    }

    @Test
    void should_throw_when_value_is_blank() {
        //when & then
        assertThrows(
                ManifestUriConfigurationException.class,
                () -> parser.parse(" ")
        );
    }

    @Test
    void should_throw_when_value_is_null() {
        //when & then
        assertThrows(
                ManifestUriConfigurationException.class,
                () -> parser.parse(null)
        );
    }
}
