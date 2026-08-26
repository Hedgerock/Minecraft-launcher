package com.launcher.api.manifest.library;

import com.launcher.model.manifest.LibraryEntry;
import com.launcher.model.manifest.RuntimeLibraryMetadata;

import java.util.List;
import java.util.Objects;

public final class DefaultRuntimeLibrarySelector implements RuntimeLibrarySelector {

    @Override
    public List<LibraryEntry> select(List<RuntimeLibraryMetadata> libraries) {
        Objects.requireNonNull(libraries, "libraries");

        return libraries.stream()
                .map(this::toLibraryEntry)
                .toList();
    }

    private LibraryEntry toLibraryEntry(RuntimeLibraryMetadata library) {
        return new LibraryEntry(
                library.path(),
                library.sha256(),
                library.size(),
                library.url()
        );
    }
}
