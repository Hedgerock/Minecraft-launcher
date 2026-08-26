package com.launcher.core.architecture.support.recording;

import com.launcher.core.architecture.support.model.TestDefaultGameClasspathBuilderResourcePathResolverRecord;
import com.launcher.core.resource.ResourcePathResolver;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class RecordingResourcePathResolver implements ResourcePathResolver {
    private final List<TestDefaultGameClasspathBuilderResourcePathResolverRecord> records = new ArrayList<>();
    private final List<Path> resolvedPaths = new ArrayList<>();

    @Override
    public Path resolve(Path baseDirectory, String resourcePath) {
        TestDefaultGameClasspathBuilderResourcePathResolverRecord record =
                new TestDefaultGameClasspathBuilderResourcePathResolverRecord(baseDirectory, resourcePath);

        records.add(record);

        Path result = baseDirectory.resolve(resourcePath);

        resolvedPaths.add(result);

        return result;
    }

    public List<Path> getResolvedPaths() {
        return List.copyOf(resolvedPaths);
    }

    public List<TestDefaultGameClasspathBuilderResourcePathResolverRecord> getRecords() {
        return List.copyOf(records);
    }
}
