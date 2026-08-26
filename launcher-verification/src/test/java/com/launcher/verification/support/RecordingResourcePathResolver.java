package com.launcher.verification.support;

import com.launcher.core.resource.ResourcePathResolver;
import com.launcher.verification.support.model.TestResourcePathResolverRecord;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RecordingResourcePathResolver implements ResourcePathResolver {
    List<TestResourcePathResolverRecord> resourcePathResolverRecords = new ArrayList<>();
    private final Path resolvedPath;
    private boolean returnResolvedPath = false;

    public RecordingResourcePathResolver(Path resolvedPath) {
        this.resolvedPath = resolvedPath;
    }

    public void setReturnResolvedPath(boolean returnResolvedPath) {
        this.returnResolvedPath = returnResolvedPath;
    }

    @Override
    public Path resolve(Path baseDirectory, String resourcePath) {
        TestResourcePathResolverRecord record = new TestResourcePathResolverRecord(baseDirectory, resourcePath);
        resourcePathResolverRecords.add(record);

        return returnResolvedPath ? resolvedPath : baseDirectory.resolve(resourcePath);
    }

    public List<TestResourcePathResolverRecord> getResourcePathResolverRecords() {
        return List.copyOf(resourcePathResolverRecords);
    }
}
