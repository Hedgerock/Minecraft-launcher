package com.launcher.api.manifest.client;

import com.launcher.api.http.LauncherHttpClient;

import java.net.URI;
import java.util.Objects;

public final class HttpManifestSignatureClient implements ManifestSignatureClient {
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
        return httpClient.getBytes(signatureUri);
    }
}
