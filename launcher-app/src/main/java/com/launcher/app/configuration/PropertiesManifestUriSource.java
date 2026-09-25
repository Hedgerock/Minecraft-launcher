package com.launcher.app.configuration;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

public final class PropertiesManifestUriSource {

    public URI load(Path configurationFile) {
        Objects.requireNonNull(configurationFile, "configurationFile");

        Properties properties = new Properties();

        try (Reader reader = Files.newBufferedReader(
                configurationFile,
                StandardCharsets.UTF_8
        )) {
            properties.load(reader);
        } catch (IOException | IllegalArgumentException exception) {
            throw new ManifestUriConfigurationException(
                    "Failed to read manifest configuration",
                    exception
            );
        }

        String value = properties.getProperty("manifest.uri");

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
