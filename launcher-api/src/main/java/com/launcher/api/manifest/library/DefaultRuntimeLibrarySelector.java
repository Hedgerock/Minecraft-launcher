package com.launcher.api.manifest.library;

import com.launcher.model.manifest.LibraryArtifactMetadata;
import com.launcher.model.manifest.LibraryEntry;
import com.launcher.model.manifest.RuntimeLibraryMetadata;
import com.launcher.model.manifest.RuntimeLibrarySelection;
import com.launcher.model.manifest.rules.LibraryRuleAction;
import com.launcher.model.runtime.RuntimeEnvironment;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class DefaultRuntimeLibrarySelector implements RuntimeLibrarySelector {

    @Override
    public RuntimeLibrarySelection select(
            List<RuntimeLibraryMetadata> libraries,
            RuntimeEnvironment environment
    ) {
        Objects.requireNonNull(libraries, "libraries");
        Objects.requireNonNull(environment, "environment");

        List<RuntimeLibraryMetadata> selectedLibraries = getSelectedLibraries(libraries, environment);
        List<LibraryEntry> librariesList = getLibraries(selectedLibraries);
        List<LibraryEntry> nativeArtifacts = getNativeArtifacts(selectedLibraries, environment);

        return new RuntimeLibrarySelection(
                librariesList,
                nativeArtifacts
        );
    }

    private List<LibraryEntry> getNativeArtifacts(
            List<RuntimeLibraryMetadata> selectedLibraries,
            RuntimeEnvironment environment
    ) {
        return selectedLibraries.stream()
                .flatMap(library -> selectedNativeArtifacts(library, environment))
                .map(this::toLibraryEntry)
                .toList();
    }

    private List<LibraryEntry> getLibraries(
            List<RuntimeLibraryMetadata> selectedLibraries
    ) {
        return selectedLibraries.stream()
                .map(RuntimeLibraryMetadata::artifact)
                .map(this::toLibraryEntry)
                .toList();
    }

    private List<RuntimeLibraryMetadata> getSelectedLibraries(
            List<RuntimeLibraryMetadata> libraries,
            RuntimeEnvironment environment
    ) {
        return libraries.stream()
                .filter(library -> shouldSelect(library, environment))
                .toList();
    }

    private boolean shouldSelect(
            RuntimeLibraryMetadata libraryMetadata,
            RuntimeEnvironment environment
    ) {
        if (libraryMetadata.rules().isEmpty()) {
            return true;
        }

        return libraryMetadata.rules().stream()
                .filter(rule -> rule.operatingSystem() == environment.operatingSystem())
                .reduce((prev, cur) -> cur)
                .map(rule -> rule.action() == LibraryRuleAction.ALLOW)
                .orElse(false);
    }

    private Stream<LibraryArtifactMetadata> selectedNativeArtifacts(
            RuntimeLibraryMetadata library,
            RuntimeEnvironment environment
    ) {
        return library.natives()
                .classifierFor(environment.operatingSystem())
                .map(classifierName -> resolveClassifierArtifact(library, classifierName))
                .stream();
    }

    private LibraryArtifactMetadata resolveClassifierArtifact(
            RuntimeLibraryMetadata library,
            String classifierName
    ) {
        LibraryArtifactMetadata artifact = library.classifiers().artifacts().get(classifierName);

        if (artifact == null) {
            throw new IllegalArgumentException(
                    "Native classifier artifact not found: " + classifierName
            );
        }

        return artifact;
    }

    private LibraryEntry toLibraryEntry(LibraryArtifactMetadata artifact) {
        return new LibraryEntry(
                artifact.path(),
                artifact.sha256(),
                artifact.size(),
                artifact.url()
        );
    }
}
