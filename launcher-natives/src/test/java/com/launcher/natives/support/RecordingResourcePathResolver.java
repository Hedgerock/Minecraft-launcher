package com.launcher.natives.support;

import com.launcher.core.resource.ResourcePathResolver;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class RecordingResourcePathResolver implements ResourcePathResolver {
    private final List<Path> baseDirectories = new ArrayList<>();
    private final List<String> resourcePaths = new ArrayList<>();

    @Override
    public Path resolve(Path baseDirectory, String resourcePath) {
        baseDirectories.add(baseDirectory);
        resourcePaths.add(resourcePath);

        return baseDirectory.resolve(resourcePath);
    }

    public List<Path> getBaseDirectories() {
        return List.copyOf(baseDirectories);
    }

    public List<String> getResourcePaths() {
        return List.copyOf(resourcePaths);
    }
}
