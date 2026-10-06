package com.launcher.downloader.download;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

final class ManagedHttpDownloadSource implements DownloadSource {
    private final ResponseFetcher fetcher;

    ManagedHttpDownloadSource() {
        this(defaultFetcher());
    }

    ManagedHttpDownloadSource(ResponseFetcher fetcher) {
        this.fetcher = Objects.requireNonNull(
                fetcher,
                "fetcher"
        );
    }

    @Override
    public InputStream open(String url) throws IOException {
        URI uri = requireHttpsUri(url);

        ResponseFetcher.Response response;

        try {
            response = fetcher.fetch(uri);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Managed resource download interrupted", e);
        }

        if (response.statusCode() != 200) {
            try (InputStream ignored = response.body()) {
                throw new IOException(
                        "Unexpected managed resource HTTP status: " + response.statusCode()
                );
            }
        }

        return response.body();
    }

    private static ResponseFetcher defaultFetcher() {
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();

        return uri -> {
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .GET()
                    .build();

            HttpResponse<InputStream> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofInputStream()
            );

            return new ResponseFetcher.Response(
                    response.statusCode(),
                    response.body()
            );
        };
    }

    private static URI requireHttpsUri(String url) throws IOException {
        if (url == null) {
            throw new IOException("Invalid managed resource URI");
        }

        try {
            URI uri = URI.create(url);

            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
                throw new IOException("Managed resource requires an HTTPS URI");
            }

            return uri;
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid managed resource URI");
        }
    }
}
