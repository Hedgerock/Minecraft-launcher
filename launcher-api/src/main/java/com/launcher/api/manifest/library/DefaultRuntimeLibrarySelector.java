package com.launcher.api.manifest.library;

import com.launcher.model.manifest.LibraryArtifactMetadata;
import com.launcher.model.manifest.LibraryEntry;
import com.launcher.model.manifest.RuntimeLibraryMetadata;
import com.launcher.model.manifest.rules.LibraryRuleAction;
import com.launcher.model.runtime.RuntimeEnvironment;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class DefaultRuntimeLibrarySelector implements RuntimeLibrarySelector {

    @Override
    public List<LibraryEntry> select(
            List<RuntimeLibraryMetadata> libraries,
            RuntimeEnvironment environment
    ) {
        Objects.requireNonNull(libraries, "libraries");
        Objects.requireNonNull(environment, "environment");

        return libraries.stream()
                .filter(library -> shouldSelect(library, environment))
                .flatMap(library -> selectedArtifacts(library, environment))
                .map(this::toLibraryEntry)
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

    private Stream<LibraryArtifactMetadata> selectedArtifacts(
            RuntimeLibraryMetadata library,
            RuntimeEnvironment environment
    ) {
        Stream<LibraryArtifactMetadata> mainArtifact = Stream.of(library.artifact());

        Stream<LibraryArtifactMetadata> nativeArtifact = library.natives()
                .classifierFor(environment.operatingSystem())
                .map(classifierName -> resolveClassifierArtifact(library, classifierName))
                .stream();

        return Stream.concat(mainArtifact, nativeArtifact);
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
