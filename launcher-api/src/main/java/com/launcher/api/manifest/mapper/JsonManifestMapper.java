package com.launcher.api.manifest.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launcher.api.manifest.exception.ManifestMappingException;
import com.launcher.api.manifest.library.RuntimeLibrarySelector;
import com.launcher.api.manifest.mapper.dto.ManifestJson;
import com.launcher.api.manifest.mapper.dto.ManifestJsonConverter;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.runtime.RuntimeEnvironment;

import java.util.Objects;

public class JsonManifestMapper implements ManifestMapper {
    private final ObjectMapper objectMapper;
    private final RuntimeLibrarySelector librarySelector;
    private final ManifestJsonConverter converter;
    private final RuntimeEnvironment environment;

    public JsonManifestMapper(
            RuntimeLibrarySelector librarySelector,
            RuntimeEnvironment environment
    ) {
        this(
                new ObjectMapper(),
                Objects.requireNonNull(librarySelector, "librarySelector"),
                new ManifestJsonConverter(),
                environment
        );
    }

    JsonManifestMapper(
            ObjectMapper objectMapper,
            RuntimeLibrarySelector librarySelector,
            ManifestJsonConverter converter,
            RuntimeEnvironment environment
    ) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.librarySelector = Objects.requireNonNull(librarySelector, "librarySelector");
        this.converter = Objects.requireNonNull(converter, "converter");
        this.environment = Objects.requireNonNull(environment, "environment");
    }

    @Override
    public Manifest map(String json) {
        try {
            ManifestJson manifestJson = objectMapper.readValue(json, ManifestJson.class);

            return converter.toManifest(manifestJson, librarySelector, environment);
        } catch (JsonProcessingException e) {
            throw new ManifestMappingException(
                    "Failed to parse manifest json",
                    e
            );
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new ManifestMappingException(
                    "Invalid manifest json",
                    e
            );
        }
    }
}
