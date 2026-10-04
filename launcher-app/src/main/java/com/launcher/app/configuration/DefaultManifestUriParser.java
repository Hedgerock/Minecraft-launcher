package com.launcher.app.configuration;

import java.net.URI;

final class DefaultManifestUriParser implements ManifestUriParser {

    @Override
    public URI parseManaged(String value) {
        URI uri = getUri(value);

        String scheme = uri.getScheme();
        String host = uri.getHost();

        boolean httpScheme = "https".equalsIgnoreCase(scheme);

        if (!httpScheme || host == null) {
            throw new ManifestUriConfigurationException(
                    "URI must be an absolute HTTPS URI with a host for managed"
            );
        }

        return uri;
    }

    @Override
    public URI parse(String value) {
        URI uri = getUri(value);

        String scheme = uri.getScheme();
        String host = uri.getHost();

        boolean httpScheme =
                "http".equalsIgnoreCase(scheme) ||
                        "https".equalsIgnoreCase(scheme);

        if (!httpScheme || host == null) {
            throw new ManifestUriConfigurationException(
                    "Manifest URI must be an absolute HTTP(S) URI with a host"
            );
        }

        return uri;
    }

    private URI getUri(String value) {
        if (value == null || value.isBlank()) {
            throw new ManifestUriConfigurationException(
                    "Manifest URI is not configured"
            );
        }

        URI uri;

        try {
            return URI.create(value.trim());
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ManifestUriConfigurationException(
                    "Manifest URI is invalid",
                    illegalArgumentException
            );
        }
    }
}
