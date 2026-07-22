package com.launcher.api.manifest.client;

import com.launcher.api.http.HttpClient;

import java.net.URI;

public class HttpManifestClient implements ManifestClient {
    private final HttpClient httpClient;
    private final URI manifestUri;

    public HttpManifestClient(HttpClient httpClient, URI manifestUri) {
        this.httpClient = httpClient;
        this.manifestUri = manifestUri;
    }

    @Override
    public String download() {

        System.out.println("Downloading manifest....");

        return httpClient.get(this.manifestUri);
    }
}
