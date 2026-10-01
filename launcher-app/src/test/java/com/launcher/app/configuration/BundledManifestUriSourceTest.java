package com.launcher.app.configuration;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BundledManifestUriSourceTest {

    @Test
    void should_return_uri_from_properties() {
        //given
        BundledManifestUriSource source = new BundledManifestUriSource(
                new DefaultManifestUriParser(),
                () -> new ByteArrayInputStream(
                        "manifest.uri=https://example.org/manifest.json"
                                .getBytes(StandardCharsets.UTF_8)
                )
        );

        //when
        URI result = source.load();

        //then
        assertEquals(
                URI.create("https://example.org/manifest.json"),
                result
        );
    }

    @Test
    void should_fail_when_property_is_empty() {
        //given
        InputStream stream = new ByteArrayInputStream(
                "manifest.uri= "
                        .getBytes(StandardCharsets.UTF_8)
        );

        BundledManifestUriSource source =
                new BundledManifestUriSource(
                        new DefaultManifestUriParser(),
                        () -> stream
                );

        //when & then
        ManifestUriConfigurationException exception = assertThrows(
                ManifestUriConfigurationException.class,
                source::load
        );

        assertEquals(
                "Manifest URI is not configured",
                exception.getMessage()
        );
    }

    @Test
    void should_fail_when_property_is_missing() {
        //given
        InputStream stream = new ByteArrayInputStream(
                "manifest.name=keystone-manifest"
                        .getBytes(StandardCharsets.UTF_8)
        );

        BundledManifestUriSource source =
                new BundledManifestUriSource(
                        new DefaultManifestUriParser(),
                        () -> stream
                );

        //when & then
        ManifestUriConfigurationException exception = assertThrows(
                ManifestUriConfigurationException.class,
                source::load
        );

        assertEquals(
                "Manifest URI is not configured",
                exception.getMessage()
        );
    }

    @Test
    void should_fail_when_property_contains_unsupported_uri() {
        //given
        InputStream stream = new ByteArrayInputStream(
                "manifest.uri=ftp://example.org/manifest.json"
                        .getBytes(StandardCharsets.UTF_8)
        );

        BundledManifestUriSource source =
                new BundledManifestUriSource(
                        new DefaultManifestUriParser(),
                        () -> stream
                );

        //when & then
        ManifestUriConfigurationException exception = assertThrows(
                ManifestUriConfigurationException.class,
                source::load
        );

        assertEquals(
                "Manifest URI must be an absolute HTTP(S) URI with a host",
                exception.getMessage()
        );
    }

    @Test
    void should_fail_when_property_contains_invalid_unicode_escape() {
        //given
        InputStream stream = new ByteArrayInputStream(
                "manifest.uri=\\uZZZZ"
                        .getBytes(StandardCharsets.UTF_8)
        );

        BundledManifestUriSource source =
                new BundledManifestUriSource(
                        new DefaultManifestUriParser(),
                        () -> stream
                );

        //when & then
        ManifestUriConfigurationException exception = assertThrows(
                ManifestUriConfigurationException.class,
                source::load
        );

        assertEquals("Failed to read bundled manifest properties", exception.getMessage());
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    void should_fail_when_input_stream_does_not_return_required_key() {
        //given
        BundledManifestUriSource source = new BundledManifestUriSource(
                new DefaultManifestUriParser(),
                InputStream::nullInputStream
        );

        //when & then
        ManifestUriConfigurationException exception = assertThrows(
                ManifestUriConfigurationException.class,
                source::load
        );

        assertEquals(
                "Manifest URI is not configured",
                exception.getMessage()
        );
    }

    @Test
    void should_fail_when_input_stream_is_unreadable() {
        //given
        InputStream failingInputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Bad read");
            }
        };

        BundledManifestUriSource source = new BundledManifestUriSource(
                new DefaultManifestUriParser(),
                () -> failingInputStream
        );

        //when & then
        ManifestUriConfigurationException exception = assertThrows(
                ManifestUriConfigurationException.class,
                source::load
        );

        assertEquals(
                "Failed to read bundled manifest properties",
                exception.getMessage()
        );

        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    void should_reject_null_input_stream_value() {
        //given
        BundledManifestUriSource source = new BundledManifestUriSource(
                new DefaultManifestUriParser(),
                () -> null
        );

        //when & then
        ManifestUriConfigurationException exception = assertThrows(
                ManifestUriConfigurationException.class,
                source::load
        );

        assertEquals(
                "Bundled manifest properties are unavailable",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_properties_resolver() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new BundledManifestUriSource(
                        new DefaultManifestUriParser(),
                        null
                )
        );

        assertEquals("propertiesResolver", exception.getMessage());
    }

    @Test
    void should_reject_manifest_uri_parser() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new BundledManifestUriSource(
                        null,
                        InputStream::nullInputStream
                )
        );

        assertEquals("manifestUriParser", exception.getMessage());
    }
}
