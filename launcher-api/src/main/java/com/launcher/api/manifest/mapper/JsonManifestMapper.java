package com.launcher.api.manifest.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launcher.api.manifest.exception.ManifestMappingException;
import com.launcher.api.manifest.mapper.dto.ManifestJson;
import com.launcher.model.manifest.Manifest;

public class JsonManifestMapper implements ManifestMapper {
    private final ObjectMapper objectMapper;

    public JsonManifestMapper() {
        this(new ObjectMapper());
    }

    JsonManifestMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Manifest map(String json) {
        try {
            ManifestJson manifestJson = objectMapper.readValue(json, ManifestJson.class);

            return manifestJson.toManifest();
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
