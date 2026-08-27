package com.launcher.api.manifest.support;

import com.launcher.api.manifest.library.RuntimeLibrarySelector;
import com.launcher.model.manifest.LibraryEntry;
import com.launcher.model.manifest.RuntimeLibraryMetadata;
import com.launcher.model.runtime.RuntimeEnvironment;

import java.util.List;

public final class RecordingRuntimeLibrarySelector implements RuntimeLibrarySelector {
    private List<RuntimeLibraryMetadata> libraries;
    private RuntimeEnvironment environment;

    @Override
    public List<LibraryEntry> select(
            List<RuntimeLibraryMetadata> libraries,
            RuntimeEnvironment environment
    ) {
        this.libraries = libraries;
        this.environment = environment;

        return List.of();
    }

    public List<RuntimeLibraryMetadata> getLibraries() {
        return List.copyOf(libraries);
    }

    public RuntimeEnvironment getEnvironment() {
        return environment;
    }
}
