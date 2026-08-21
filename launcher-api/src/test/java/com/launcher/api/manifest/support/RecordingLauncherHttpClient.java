package com.launcher.api.manifest.support;

import com.launcher.api.http.LauncherHttpClient;

import java.net.URI;

public final class RecordingLauncherHttpClient implements LauncherHttpClient {
    private String response = "{}";
    private URI uri;

    @Override
    public String get(URI uri) {
        this.uri = uri;
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public URI getUri() {
        return uri;
    }

    public String getResponse() {
        return response;
    }
}
