package com.launcher.app.configuration;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ManagedManifestUrisTest {

    @Test
    void should_reject_null_host() {
        //given
        URI manifestUri = URI.create("https://example.org/manifest.json");
        URI signatureUri = URI.create("https://:3000/manifest.sig");

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> new ManagedManifestUris(manifestUri, signatureUri)
        );

        assertEquals(
                "host",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_scheme() {
        //given
        URI manifestUri = URI.create("example.org/manifest.json");
        URI signatureUri = URI.create("https://example.org/manifest.sig");

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> new ManagedManifestUris(manifestUri, signatureUri)
        );

        assertEquals(
                "scheme",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_non_https_signature_uri() {
        //given
        URI manifestUri = URI.create("https://example.org/manifest.json");
        URI signatureUri = URI.create("http://example.org/manifest.sig");

        //when & then
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class,
                        () -> new ManagedManifestUris(manifestUri, signatureUri)
                );

        assertEquals(
                "URI scheme must be https: signatureUri",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_non_https_manifest_uri() {
        //given
        URI manifestUri = URI.create("http://example.org/manifest.json");
        URI signatureUri = URI.create("https://example.org/manifest.sig");

        //when & then
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class,
                        () -> new ManagedManifestUris(manifestUri, signatureUri)
                );

        assertEquals(
                "URI scheme must be https: manifestUri",
                exception.getMessage()
        );
    }

    @Test
    void should_create_managed_manifest_uris() {
        //given
        URI expectedManifestUri = URI.create("https://example.org/manifest.json");
        URI expectedSignatureUri = URI.create("https://example.org/manifest.sig");

        //when
        ManagedManifestUris managedManifestUris =
                new ManagedManifestUris(expectedManifestUri, expectedSignatureUri);

        //then
        assertEquals(
                expectedManifestUri,
                managedManifestUris.manifestUri()
        );

        assertEquals(
                expectedSignatureUri,
                managedManifestUris.signatureUri()
        );
    }

    @Test
    void should_reject_null_signature_uri() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new ManagedManifestUris(
                        URI.create("https://example.org/manifest.json"),
                        null
                )
        );

        assertEquals(
                "signatureUri",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_manifest_uri() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new ManagedManifestUris(
                        null,
                        URI.create("https://example.org/manifest.sig")
                )
        );

        assertEquals(
                "manifestUri",
                exception.getMessage()
        );
    }
}
