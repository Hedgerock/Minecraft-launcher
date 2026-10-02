package com.launcher.api.manifest.client;

import com.launcher.api.manifest.support.RecordingLauncherHttpClient;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpManifestClientTest {
    private URI getUri() {
        return URI.create("https://example.com/launcher/manifest.json");
    }

    @Test
    void should_reject_null_manifest_uri() {
        //given
        RecordingLauncherHttpClient launcherHttpClient = new RecordingLauncherHttpClient();

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new HttpManifestClient(launcherHttpClient, null)
        );

        assertTrue(exception.getMessage().contains("manifestUri"));
    }

    @Test
    void should_reject_null_http_client() {
        //given
        URI manifestUri = getUri();

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new HttpManifestClient(null, manifestUri)
        );

        assertTrue(exception.getMessage().contains("launcherHttpClient"));
    }

    @Test
    void should_download_byte_result_from_configured_uri() {
        //given
        URI manifestUri = getUri();
        RecordingLauncherHttpClient launcherHttpClient = new RecordingLauncherHttpClient();
        byte[] expectedBytes =
                """
                {
                  "minecraftVersion": "1.12.2"
                }
                """.getBytes(StandardCharsets.UTF_8);

        launcherHttpClient.setResponse(expectedBytes);

        HttpManifestClient manifestClient = new HttpManifestClient(launcherHttpClient, manifestUri);

        //when
        byte[] result = manifestClient.downloadBytes();

        //then
        assertEquals(manifestUri, launcherHttpClient.getUri());
        assertArrayEquals(expectedBytes, result);
    }

    @Test
    void should_download_manifest_json_from_configured_uri() {
        //given
        URI manifestUri = getUri();
        RecordingLauncherHttpClient launcherHttpClient = new RecordingLauncherHttpClient();
        launcherHttpClient.setResponse(
                """
                {
                  "minecraftVersion": "1.12.2"
                }
                """
        );

        HttpManifestClient manifestClient = new HttpManifestClient(launcherHttpClient, manifestUri);

        //when
        String result = manifestClient.download();

        //then
        assertEquals(manifestUri, launcherHttpClient.getUri());
        assertEquals(launcherHttpClient.getResponse(), result);
    }
}
