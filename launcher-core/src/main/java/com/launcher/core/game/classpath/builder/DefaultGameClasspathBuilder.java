package com.launcher.core.game.classpath.builder;

import com.launcher.core.game.classpath.GameClasspath;
import com.launcher.core.resource.ResourcePathResolver;
import com.launcher.model.manifest.LaunchInfo;
import com.launcher.model.manifest.LibraryEntry;
import com.launcher.model.manifest.Manifest;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DefaultGameClasspathBuilder implements GameClasspathBuilder {
    private final ResourcePathResolver resourcePathResolver;

    public DefaultGameClasspathBuilder(ResourcePathResolver resourcePathResolver) {
        this.resourcePathResolver = Objects.requireNonNull(resourcePathResolver, "resourcePathResolver");
    }

    @Override
    public GameClasspath build(
            Manifest manifest,
            List<LibraryEntry> libraries,
            Path gameDirectory
    ) {
        Objects.requireNonNull(manifest, "manifest");
        Objects.requireNonNull(libraries, "libraries");
        libraries.forEach(library -> Objects.requireNonNull(library, "library"));
        Objects.requireNonNull(gameDirectory, "gameDirectory");

        LaunchInfo launchInfo = manifest.launchInfo();
        String clientArtifactPath = launchInfo.clientArtifactPath();

        List<String> entries = resolveEntries(manifest, libraries, clientArtifactPath);

        return new GameClasspath(
                entries.stream()
                        .map(entry ->
                                resourcePathResolver.resolve(gameDirectory, entry)
                        )
                        .toList()
        );
    }

    private List<String> resolveEntries(
            Manifest manifest,
            List<LibraryEntry> libraries,
            String clientArtifactPath
    ) {
        List<String> entries;

        if (clientArtifactPath != null) {
            boolean declared = manifest.files().stream()
                    .anyMatch(file -> clientArtifactPath.equals(file.path()));

            if (!declared) {
                throw new IllegalArgumentException(
                        "Client artifact path is not declared in manifest files: " +
                                clientArtifactPath
                );
            }

            entries = new ArrayList<>(
                    libraries.stream()
                            .map(LibraryEntry::path)
                            .toList()
            );
            entries.add(clientArtifactPath);
        } else {
            entries = libraries.isEmpty()
                    ? manifest.launchInfo().classpath()
                    : libraries.stream()
                    .map(LibraryEntry::path)
                    .toList();
        }

        return entries;
    }
}
