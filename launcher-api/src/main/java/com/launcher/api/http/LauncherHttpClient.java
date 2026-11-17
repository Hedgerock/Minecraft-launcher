package com.launcher.api.http;

import java.net.URI;

public interface LauncherHttpClient {
    String get(URI uri, HttpRequestLimits limits);
    byte[] getBytes(URI uri, HttpRequestLimits limits);
}
