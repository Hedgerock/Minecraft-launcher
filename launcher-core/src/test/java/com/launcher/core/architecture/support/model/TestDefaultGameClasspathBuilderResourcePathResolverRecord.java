package com.launcher.core.architecture.support.model;

import java.nio.file.Path;

public record TestDefaultGameClasspathBuilderResourcePathResolverRecord(
        Path baseDirectory,
        String resourcePath
) {
}
