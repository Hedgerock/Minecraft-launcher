package com.launcher.core.download.model;

import com.launcher.model.manifest.ResourceEntry;

import java.util.List;
import java.util.Objects;

public record DownloadPlan(
        List<ResourceEntry> resources
) {

    public DownloadPlan {
        Objects.requireNonNull(resources, "resources");

        resources = List.copyOf(resources);
    }
    public boolean isEmpty() {
        return resources.isEmpty();
    }

}
