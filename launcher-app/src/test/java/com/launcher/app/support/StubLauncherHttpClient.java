package com.launcher.app.support;

import com.launcher.api.http.LauncherHttpClient;

import java.net.URI;

public final class StubLauncherHttpClient implements LauncherHttpClient {
    private final URI manifestUri;
    private final byte[] manifestBytes;
    private final URI signatureUri;
    private final byte[] signatureBytes;

    public StubLauncherHttpClient(
            URI manifestUri,
            byte[] manifestBytes,
            URI signatureUri,
            byte[] signatureBytes
    ) {
        this.manifestUri = manifestUri;
        this.manifestBytes = manifestBytes;
        this.signatureUri = signatureUri;
        this.signatureBytes = signatureBytes;
    }

    @Override
    public String get(URI uri) {
        throw new AssertionError(
                "Unexpected URI: " + uri
        );
    }

    @Override
    public byte[] getBytes(URI uri) {
        if (manifestUri.equals(uri)) {
            return manifestBytes.clone();
        }

        if (signatureUri.equals(uri)) {
            return signatureBytes.clone();
        }

        throw new AssertionError(
                "Unexpected URI: " + uri
        );
    }
}
