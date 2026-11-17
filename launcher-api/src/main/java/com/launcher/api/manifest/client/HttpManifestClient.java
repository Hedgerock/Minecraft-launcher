package com.launcher.api.manifest.client;

import com.launcher.api.http.HttpRequestLimits;
import com.launcher.api.http.LauncherHttpClient;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

public class HttpManifestClient implements ManifestClient {
    private static final HttpRequestLimits MANIFEST_LIMITS =
            new HttpRequestLimits(Duration.ofSeconds(30), 32 * 1024 * 1024);

    private final LauncherHttpClient launcherHttpClient;
    private final URI manifestUri;

    public HttpManifestClient(LauncherHttpClient launcherHttpClient, URI manifestUri) {
        this.launcherHttpClient = Objects.requireNonNull(
                launcherHttpClient,
                "launcherHttpClient"
        );

        this.manifestUri = Objects.requireNonNull(
                manifestUri,
                "manifestUri"
        );
    }

    @Override
    public String download() {
        return launcherHttpClient.get(this.manifestUri, MANIFEST_LIMITS);
    }

    @Override
    public byte[] downloadBytes() {
        return launcherHttpClient.getBytes(this.manifestUri, MANIFEST_LIMITS);
    }
}
