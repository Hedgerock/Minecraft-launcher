package com.launcher.downloader.support.model;

import java.nio.file.Path;

public record TestDownloadServiceResourcePathResolverRecord(
        Path baseDirectory,
        String resourcePath
) {
}
