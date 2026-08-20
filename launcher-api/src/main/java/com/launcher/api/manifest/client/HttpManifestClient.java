package com.launcher.api.manifest.client;

import com.launcher.api.http.LauncherHttpClient;

import java.net.URI;

public class HttpManifestClient implements ManifestClient {
    private final LauncherHttpClient launcherHttpClient;
    private final URI manifestUri;

    public HttpManifestClient(LauncherHttpClient launcherHttpClient, URI manifestUri) {
        this.launcherHttpClient = launcherHttpClient;
        this.manifestUri = manifestUri;
    }

    @Override
    public String download() {
        return launcherHttpClient.get(this.manifestUri);
    }
}
