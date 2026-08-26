package com.launcher.verification.support.model;

import java.nio.file.Path;

public record TestResourcePathResolverRecord(
        Path baseDirectory,
        String resourcePath
) {
}
