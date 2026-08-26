package com.launcher.core.resource;

import java.nio.file.Path;
import java.util.Objects;

public class SafeResourcePathResolver implements ResourcePathResolver {

    @Override
    public Path resolve(Path baseDirectory, String resourcePath) {
        Objects.requireNonNull(baseDirectory, "baseDirectory");
        Objects.requireNonNull(resourcePath, "resourcePath");

        if (resourcePath.isBlank()) {
            throw new UnsafeResourcePathException(
                    "Resource path must not be blank"
            );
        }

        Path resource = Path.of(resourcePath);

        if (resource.isAbsolute()) {
            throw new UnsafeResourcePathException(
                    "Resource path must not be absolute"
            );
        }

        Path normalizedBase = baseDirectory.normalize();

        Path normalizedPath = normalizedBase.resolve(resource).normalize();

        if (!normalizedPath.startsWith(normalizedBase)) {
            throw new UnsafeResourcePathException(
                    "Resource path escapes base directory"
            );
        }

        return normalizedPath;
    }
}
