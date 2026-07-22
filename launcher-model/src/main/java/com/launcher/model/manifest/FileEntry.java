package com.launcher.model.manifest;

public record FileEntry(
        String path,
        String sha256,
        long size,
        String url
) {
}
