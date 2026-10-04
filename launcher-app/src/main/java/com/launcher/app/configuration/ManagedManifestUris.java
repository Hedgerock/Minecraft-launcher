package com.launcher.app.configuration;

import java.net.URI;
import java.util.Objects;

public record ManagedManifestUris(
        URI manifestUri,
        URI signatureUri
) {

    public ManagedManifestUris {
        Objects.requireNonNull(manifestUri, "manifestUri");
        Objects.requireNonNull(signatureUri, "signatureUri");

        isHttps(manifestUri, "manifestUri");
        isHttps(signatureUri, "signatureUri");
    }

    private void isHttps(URI uri, String value) {
        String scheme = Objects.requireNonNull(uri.getScheme(), "scheme");
        String host = Objects.requireNonNull(uri.getHost(), "host");

        if (!"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("URI scheme must be https: %s".formatted(value));
        }
    }
}
