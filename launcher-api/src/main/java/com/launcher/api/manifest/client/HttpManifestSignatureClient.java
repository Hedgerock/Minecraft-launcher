package com.launcher.api.manifest.client;

import com.launcher.api.http.HttpRequestLimits;
import com.launcher.api.http.LauncherHttpClient;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

public final class HttpManifestSignatureClient implements ManifestSignatureClient {
    private static final HttpRequestLimits SIGNATURE_LIMITS =
            new HttpRequestLimits(Duration.ofSeconds(10), 64);

    private final LauncherHttpClient httpClient;
    private final URI signatureUri;

    public HttpManifestSignatureClient(
            LauncherHttpClient httpClient,
            URI signatureUri
    ) {
        this.httpClient = Objects.requireNonNull(
                httpClient,
                "httpClient"
        );
        this.signatureUri = Objects.requireNonNull(
                signatureUri,
                "signatureUri"
        );
    }

    @Override
    public byte[] download() {
        return httpClient.getBytes(signatureUri, SIGNATURE_LIMITS);
    }
}
