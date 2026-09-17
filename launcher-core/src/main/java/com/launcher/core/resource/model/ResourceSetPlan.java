package com.launcher.core.resource.model;

import java.util.List;
import java.util.Objects;

public record ResourceSetPlan(
        List<PlannedResource> resources
) {
    public ResourceSetPlan {
        Objects.requireNonNull(resources, "resources");

        resources.forEach(
                resource ->
                        Objects.requireNonNull(resource, "resource")
        );

        resources = List.copyOf(resources);
    }
}
