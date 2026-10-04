package com.launcher.app.configuration;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BundledManifestPublicKeySourceTest {

    @Test
    void should_fail_when_resolver_value_is_missing() {
        //given
        InputStream nullInputStream = null;

        BundledManifestPublicKeySource source =
                new BundledManifestPublicKeySource(() -> nullInputStream);

        //when & then
        ManifestPublicKeyConfigurationException exception = assertThrows(
                ManifestPublicKeyConfigurationException.class,
                source::load
        );

        assertEquals(
                "Failed to load public key from bundled resource",
                exception.getMessage()
        );
    }

    @Test
    void should_fail_when_read_process_failed() {
        //given
        InputStream failingInputStream = new InputStream () {
            @Override
            public int read() throws IOException {
                throw new IOException("Failed to read");
            }
        };

        BundledManifestPublicKeySource source =
                new BundledManifestPublicKeySource(() -> failingInputStream);

        //when & then
        ManifestPublicKeyConfigurationException exception = assertThrows(
                ManifestPublicKeyConfigurationException.class,
                source::load
        );

        assertEquals(
                "Failed to load public key from bundled resource",
                exception.getMessage()
        );

        assertInstanceOf(
                IOException.class,
                exception.getCause()
        );
    }

    @Test
    void should_fail_when_public_key_value_is_not_valid() {
        //given
        String publicKeyValue =
                "MCowBQYDK2VwAyEA11qYAYKxCrfVS/7TyWbLJQADh0tp9Y0VJHVP3D9lMPg=";

        BundledManifestPublicKeySource source =
                new BundledManifestPublicKeySource(() ->
                        new ByteArrayInputStream(publicKeyValue.getBytes(StandardCharsets.UTF_8))
                );

        //when & then
        ManifestPublicKeyConfigurationException exception = assertThrows(
                ManifestPublicKeyConfigurationException.class,
                source::load
        );

        assertEquals(
                "Failed to load public key from bundled resource",
                exception.getMessage()
        );

        assertInstanceOf(
                InvalidKeySpecException.class,
                exception.getCause()
        );
    }

    @Test
    void should_create_subject_public_key_info() {
        //given
        String publicKeyValue =
                "MCowBQYDK2VwAyEA11qYAYKxCrfVS/7TyWbLJQADh0tp9Y0VJHVP3D9lMPg=";

        byte[] bytes = Base64.getDecoder()
                .decode(publicKeyValue);

        BundledManifestPublicKeySource source =
                new BundledManifestPublicKeySource(() ->
                        new ByteArrayInputStream(bytes)
                );

        //when
        PublicKey result = source.load();

        //then
        assertArrayEquals(
                bytes,
                result.getEncoded()
        );
    }

    @Test
    void should_reject_public_key_resolver() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new BundledManifestPublicKeySource(null)
        );

        assertEquals("publicKeyResolver", exception.getMessage());
    }
}
