package com.launcher.app.configuration;

import java.net.URI;

final class DefaultManifestUriParser implements ManifestUriParser {

    @Override
    public URI parse(String value) {
        if (value == null || value.isBlank()) {
            throw new ManifestUriConfigurationException(
                    "Manifest URI is not configured"
            );
        }

        URI uri;

        try {
            uri = URI.create(value.trim());
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ManifestUriConfigurationException(
                    "Manifest URI is invalid",
                    illegalArgumentException
            );
        }

        String scheme = uri.getScheme();
        boolean httpScheme =
                "http".equalsIgnoreCase(scheme) ||
                        "https".equalsIgnoreCase(scheme);

        if (!httpScheme || uri.getHost() == null) {
            throw new ManifestUriConfigurationException(
                    "Manifest URI must be an absolute HTTP(S) URI with a host"
            );
        }

        return uri;
    }
}
