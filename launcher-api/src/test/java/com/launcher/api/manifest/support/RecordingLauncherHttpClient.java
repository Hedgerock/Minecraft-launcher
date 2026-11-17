package com.launcher.api.manifest.support;

import com.launcher.api.http.HttpRequestLimits;
import com.launcher.api.http.LauncherHttpClient;

import java.net.URI;

public final class RecordingLauncherHttpClient implements LauncherHttpClient {
    private String response = "{}";
    private byte[] responseOfBytes;
    private URI uri;
    private HttpRequestLimits limits;

    @Override
    public String get(URI uri, HttpRequestLimits limits) {
        this.uri = uri;
        this.limits = limits;
        return response;
    }

    @Override
    public byte[] getBytes(URI uri, HttpRequestLimits limits) {
        this.uri = uri;
        this.limits = limits;
        return responseOfBytes;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public void setResponse(byte[] response) {
        this.responseOfBytes = response;
    }

    public URI getUri() {
        return uri;
    }

    public HttpRequestLimits getLimits() {
        return limits;
    }

    public String getResponse() {
        return response;
    }
}
