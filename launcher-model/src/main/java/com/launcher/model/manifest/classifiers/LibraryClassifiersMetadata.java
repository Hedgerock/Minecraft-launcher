package com.launcher.model.manifest.classifiers;

import com.launcher.model.manifest.LibraryArtifactMetadata;

import java.util.Map;
import java.util.Objects;

public record LibraryClassifiersMetadata(
        Map<String, LibraryArtifactMetadata> artifacts
) {

    public LibraryClassifiersMetadata {
        Objects.requireNonNull(artifacts, "artifacts");

        artifacts.forEach((classifierName, artifact) -> {
            Objects.requireNonNull(classifierName, "classifierName");
            Objects.requireNonNull(artifact, "artifact");

            if (classifierName.isBlank()) {
                throw new IllegalArgumentException("classifierName must not be blank");
            }

        });

        artifacts = Map.copyOf(artifacts);
    }

    public boolean isEmpty() {
        return artifacts.isEmpty();
    }

}
