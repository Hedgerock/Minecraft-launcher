package com.launcher.api.http;

import com.launcher.api.http.exception.HttpRequestException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

public class JavaLauncherHttpClient implements LauncherHttpClient {

    private final HttpClient httpClient;

    public JavaLauncherHttpClient() {
        this(HttpClient.newHttpClient());
    }

    JavaLauncherHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public byte[] getBytes(URI uri) {
        Objects.requireNonNull(uri, "uri");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        try {
            HttpResponse<byte[]> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofByteArray()
            );

            validateResponseStatusCode(response);

            return response.body();
        } catch (IOException e) {
            throw new HttpRequestException(
                    "HTTP GET failed",
                    e
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new HttpRequestException("HTTP GET interrupted", e);
        }
    }

    @Override
    public String get(URI uri) {
        Objects.requireNonNull(uri, "uri");

        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            validateResponseStatusCode(response);

            return response.body();

        } catch (IOException e) {
            throw new HttpRequestException("HTTP GET failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new HttpRequestException("HTTP GET interrupted", e);
        }
    }

    private <T> void validateResponseStatusCode(HttpResponse<T> response) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new HttpRequestException(
                    "HTTP GET failed with status code: " + response.statusCode()
            );
        }
    }
}
