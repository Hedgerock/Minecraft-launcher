package com.launcher.api.manifest.library;

import com.launcher.model.manifest.LibraryArtifactMetadata;
import com.launcher.model.manifest.LibraryEntry;
import com.launcher.model.manifest.RuntimeLibraryMetadata;
import com.launcher.model.manifest.rules.LibraryRuleAction;
import com.launcher.model.runtime.RuntimeEnvironment;

import java.util.List;
import java.util.Objects;

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

    private LibraryEntry toLibraryEntry(RuntimeLibraryMetadata library) {
        LibraryArtifactMetadata artifact = library.artifact();

        return new LibraryEntry(
                artifact.path(),
                artifact.sha256(),
                artifact.size(),
                artifact.url()
        );
    }
}
