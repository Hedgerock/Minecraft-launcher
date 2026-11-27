package com.launcher.preparer.source;

import com.launcher.preparer.validation.VanillaAssetResourceUriValidator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Objects;

public final class HttpVanillaAssetResourceSource {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofMinutes(2);

    private final VanillaAssetResponseFetcher fetcher;

    public HttpVanillaAssetResourceSource() {
        this(defaultFetcher());
    }

    HttpVanillaAssetResourceSource(VanillaAssetResponseFetcher fetcher) {
        this.fetcher = Objects.requireNonNull(fetcher, "fetcher");
    }

    public OpenedVanillaAssetResource open(URI uri) throws IOException {
        URI resourceUri = VanillaAssetResourceUriValidator.requireValid(uri);

        final VanillaAssetResponseFetcher.Response response;

        try {
            response = fetcher.fetch(resourceUri);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(
                    "Vanilla asset resource request interrupted",
                    e
            );
        }

        if (response.statusCode() != 200) {
            try (InputStream ignored = response.body()) {
                throw new IOException(
                        "Unexpected vanilla asset HTTP status: " + response.statusCode()
                );
            }
        }

        return new OpenedVanillaAssetResource(
                resourceUri,
                response.body()
        );
    }

    private static VanillaAssetResponseFetcher defaultFetcher() {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();

        return new DeadlineVanillaAssetResponseFetcher(client, REQUEST_TIMEOUT);
    }
}
