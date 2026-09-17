package com.launcher.core.resource;

import com.launcher.model.manifest.ResourceEntry;

import java.nio.file.Path;
import java.util.Objects;

public class ResourceSetConflictException extends RuntimeException {
    private final Path targetPath;
    private final ResourceEntry firstResource;
    private final ResourceEntry conflictingResource;

    public ResourceSetConflictException(
            Path targetPath,
            ResourceEntry firstResource,
            ResourceEntry conflictingResource,
            String message
    ) {
        super(message);
        this.targetPath = Objects.requireNonNull(targetPath, "targetPath");
        this.firstResource = Objects.requireNonNull(firstResource, "firstResource");
        this.conflictingResource = Objects.requireNonNull(conflictingResource, "conflictingResource");
    }

    public Path getTargetPath() {
        return targetPath;
    }

    public ResourceEntry getFirstResource() {
        return firstResource;
    }

    public ResourceEntry getConflictingResource() {
        return conflictingResource;
    }
}
