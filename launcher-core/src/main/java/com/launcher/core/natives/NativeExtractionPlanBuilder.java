package com.launcher.core.natives;

import com.launcher.core.natives.model.NativeExtractionPlan;
import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.model.manifest.RuntimeLibrarySelection;

import java.nio.file.Path;
import java.util.Objects;

public final class NativeExtractionPlanBuilder {

    private final DirectoryProvider directoryProvider;

    public NativeExtractionPlanBuilder(DirectoryProvider directoryProvider) {
        this.directoryProvider = Objects.requireNonNull(directoryProvider, "directoryProvider");
    }

    public NativeExtractionPlan build(RuntimeLibrarySelection selection) {
        Objects.requireNonNull(selection, "selection");

        Path nativesDirectory = directoryProvider.directories().natives();

        return new NativeExtractionPlan(
                selection.nativeArtifacts(),
                nativesDirectory
        );
    }

}
