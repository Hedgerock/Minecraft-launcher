package com.launcher.api.manifest.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launcher.api.manifest.exception.ManifestMappingException;
import com.launcher.api.manifest.library.RuntimeLibrarySelector;
import com.launcher.api.manifest.mapper.dto.ManifestJson;
import com.launcher.api.manifest.mapper.dto.ManifestJsonConverter;
import com.launcher.model.manifest.Manifest;

import java.util.Objects;

public class JsonManifestMapper implements ManifestMapper {
    private final ObjectMapper objectMapper;
    private final RuntimeLibrarySelector librarySelector;
    private final ManifestJsonConverter converter;

    public JsonManifestMapper(RuntimeLibrarySelector librarySelector) {
        this(
                new ObjectMapper(),
                Objects.requireNonNull(librarySelector, "librarySelector"),
                new ManifestJsonConverter()
        );
    }

    JsonManifestMapper(
            ObjectMapper objectMapper,
            RuntimeLibrarySelector librarySelector,
            ManifestJsonConverter converter
    ) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.librarySelector = Objects.requireNonNull(librarySelector, "librarySelector");
        this.converter = Objects.requireNonNull(converter, "converter");
    }

    @Override
    public Manifest map(String json) {
        try {
            ManifestJson manifestJson = objectMapper.readValue(json, ManifestJson.class);

            return converter.toManifest(manifestJson, librarySelector);
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
