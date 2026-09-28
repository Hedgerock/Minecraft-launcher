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
    private final ManifestUriParser parser;

    public PropertiesManifestUriSource() {
        this(new DefaultManifestUriParser());
    }

    PropertiesManifestUriSource(ManifestUriParser parser) {
        this.parser = Objects.requireNonNull(parser, "parser");
    }

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

        return parser.parse(value);
    }
}
