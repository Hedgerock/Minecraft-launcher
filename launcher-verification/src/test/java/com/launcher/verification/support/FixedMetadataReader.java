package com.launcher.verification.support;

import com.launcher.storage.file.FileMetadataReader;

import java.nio.file.Path;

public final class FixedMetadataReader implements FileMetadataReader {
    private final boolean exists;
    private final long size;

    public FixedMetadataReader(boolean exists, long size) {
        this.exists = exists;
        this.size = size;
    }

    @Override
    public boolean exists(Path path) {
        return exists;
    }

    @Override
    public long size(Path path) {
        return size;
    }
}
