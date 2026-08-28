package com.launcher.core.natives.model;

import com.launcher.model.manifest.LibraryEntry;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public record NativeExtractionPlan(
        List<LibraryEntry> artifacts,
        Path targetDirectory
) {

    public NativeExtractionPlan {
        Objects.requireNonNull(artifacts, "artifacts");
        Objects.requireNonNull(targetDirectory, "targetDirectory");

        artifacts = List.copyOf(artifacts);
    }

    public boolean isEmpty() {
        return artifacts.isEmpty();
    }

}
