package com.launcher.downloader.support;

import com.launcher.core.resource.ResourcePathResolver;
import com.launcher.downloader.support.model.TestDownloadServiceResourcePathResolverRecord;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class RecordingResourcePathResolver implements ResourcePathResolver {
    private boolean returnResolvedPath = false;
    private Path resolvedPath;
    List<TestDownloadServiceResourcePathResolverRecord> resourcePathResolverRecords = new ArrayList<>();

    public RecordingResourcePathResolver(Path resolvedPath) {
        this.resolvedPath = resolvedPath;
    }

    public void setResolvedPath(Path resolvedPath) {
        this.resolvedPath = resolvedPath;
    }

    @Override
    public Path resolve(Path baseDirectory, String resourcePath) {
        TestDownloadServiceResourcePathResolverRecord record =
                new TestDownloadServiceResourcePathResolverRecord(baseDirectory, resourcePath);

        resourcePathResolverRecords.add(record);

        return returnResolvedPath ? resolvedPath : baseDirectory.resolve(resourcePath);
    }

    public void setWithReturnResolvedPath() {
        if (returnResolvedPath) {
            return;
        }

        this.returnResolvedPath = true;
    }

    public List<TestDownloadServiceResourcePathResolverRecord> getResourcePathResolverRecords() {
        return List.copyOf(resourcePathResolverRecords);
    }
}
