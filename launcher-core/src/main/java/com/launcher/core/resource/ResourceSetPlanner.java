package com.launcher.core.resource;

import com.launcher.core.resource.model.PlannedResource;
import com.launcher.core.resource.model.ResourceSetPlan;
import com.launcher.model.manifest.ResourceEntry;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ResourceSetPlanner {
    private static final String TEMPLATE_OF_ERROR_MESSAGE =
            "Conflicting resources for target '%s': '%s' and '%s'";

    private final ResourcePathResolver resourcePathResolver;

    public ResourceSetPlanner(ResourcePathResolver resourcePathResolver) {
        this.resourcePathResolver = Objects.requireNonNull(resourcePathResolver, "resourcePathResolver");
    }

    public ResourceSetPlan plan(
            List<ResourceEntry> resources,
            Path gameDirectory
    ) {
        Objects.requireNonNull(resources, "resources");
        Objects.requireNonNull(gameDirectory, "gameDirectory");

        Map<Path, PlannedResource> plannedResourceMap = new LinkedHashMap<>();

        for (ResourceEntry resource : resources) {
            Path resourcePath = resourcePathResolver.resolve(gameDirectory, resource.path());

            if (plannedResourceMap.containsKey(resourcePath)) {
                ResourceEntry existingResource = plannedResourceMap.get(resourcePath).resource();

                if (!hasSameParameters(existingResource, resource)) {
                    throw new ResourceSetConflictException(
                            resourcePath,
                            existingResource,
                            resource,
                            TEMPLATE_OF_ERROR_MESSAGE
                                    .formatted(
                                            resourcePath,
                                            existingResource.path(),
                                            resource.path()
                                    )
                    );
                }

                continue;
            }

            plannedResourceMap.put(resourcePath, new PlannedResource(resource, resourcePath));
        }

        return new ResourceSetPlan(
                new ArrayList<>(plannedResourceMap.values())
        );
    }

    private boolean hasSameParameters(ResourceEntry existingResource, ResourceEntry resource) {
        return existingResource.sha256().equals(resource.sha256())
                        && existingResource.size() == resource.size()
                        && existingResource.url().equals(resource.url());
    }
}
