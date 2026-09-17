package com.launcher.core.resource.model;

import com.launcher.model.manifest.ResourceEntry;

import java.nio.file.Path;
import java.util.Objects;

public record PlannedResource(
        ResourceEntry resource,
        Path targetPath
) {
    public PlannedResource {
        Objects.requireNonNull(resource, "resource");
        Objects.requireNonNull(targetPath, "targetPath");
    }
}
