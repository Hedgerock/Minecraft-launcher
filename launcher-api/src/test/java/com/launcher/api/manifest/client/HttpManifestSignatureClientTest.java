package com.launcher.api.manifest.client;

import com.launcher.api.manifest.support.ManifestJsonProvider;
import com.launcher.api.manifest.support.RecordingLauncherHttpClient;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HttpManifestSignatureClientTest {

    @Test
    void should_download_signature_bytes() throws Exception {
        //given
        RecordingLauncherHttpClient httpClient = new RecordingLauncherHttpClient();
        byte[] signatureBytes = getSignatureSign();
        httpClient.setResponse(signatureBytes);

        URI expectedUri = URI.create("https//example.org/signature");

        HttpManifestSignatureClient manifestSignatureClient =
                new HttpManifestSignatureClient(
                        httpClient,
                        expectedUri
                );

        //when
        byte[] result = manifestSignatureClient.download();

        //then
        assertEquals(
                expectedUri,
                httpClient.getUri()
        );

        assertArrayEquals(
                signatureBytes,
                result
        );
    }

    @Test
    void should_reject_null_signature_uri() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new HttpManifestSignatureClient(
                        new RecordingLauncherHttpClient(),
                        null
                )
        );

        assertEquals(
                "signatureUri",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_http_client() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new HttpManifestSignatureClient(
                        null,
                        URI.create("https://example.org/signature")
                )
        );

        assertEquals(
                "httpClient",
                exception.getMessage()
        );
    }

    private byte[] getSignatureSign() throws Exception {
        KeyPair keyPair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();

        Signature signature = Signature.getInstance("Ed25519");
        signature.initSign(keyPair.getPrivate());
        signature.update(ManifestJsonProvider.getMinimumValidManifestJson()
                .getBytes(StandardCharsets.UTF_8));
        return signature.sign();
    }
}
