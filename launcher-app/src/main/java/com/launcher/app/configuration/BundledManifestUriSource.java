package com.launcher.app.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Supplier;

final class BundledManifestUriSource {
    private static final String MANIFEST_URI_PROPERTY = "manifest.uri";

    private final ManifestUriParser manifestUriParser;
    private final Supplier<InputStream> propertiesResolver;

    BundledManifestUriSource(ManifestUriParser manifestUriParser) {
        this(manifestUriParser,
                () -> BundledManifestUriSource.class
                        .getResourceAsStream("/managed-manifest.properties")
        );
    }

    BundledManifestUriSource(
            ManifestUriParser manifestUriParser,
            Supplier<InputStream> propertiesResolver
    ) {
        this.manifestUriParser = Objects.requireNonNull(
                manifestUriParser,
                "manifestUriParser"
        );
        this.propertiesResolver = Objects.requireNonNull(
                propertiesResolver,
                "propertiesResolver"
        );
    }

    URI load() {
        InputStream stream = propertiesResolver.get();

        if (stream == null) {
            throw new ManifestUriConfigurationException(
                    "Bundled manifest properties are unavailable"
            );
        }

        Properties properties = new Properties();

        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException | IllegalArgumentException e) {
            throw new ManifestUriConfigurationException(
                    "Failed to read bundled manifest properties",
                    e
            );
        }

        return manifestUriParser.parse(
                properties.getProperty(MANIFEST_URI_PROPERTY)
        );
    }
}
