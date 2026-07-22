package com.launcher.api.http;

import java.net.URI;

public class JavaHttpClient implements HttpClient{

    @Override
    public String get(URI url) {
        System.out.println("GET " + url);

        return "";
    }
}
