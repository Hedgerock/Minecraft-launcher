package com.launcher.api.manifest.mapper.dto;

import com.launcher.api.manifest.library.RuntimeLibrarySelector;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.ManifestLoadResult;
import com.launcher.model.manifest.RuntimeLibraryMetadata;
import com.launcher.model.manifest.RuntimeLibrarySelection;
import com.launcher.model.runtime.RuntimeEnvironment;

import java.util.List;

public final class ManifestJsonConverter {

    public ManifestLoadResult toManifestLoadResult(
            ManifestJson manifestJson,
            RuntimeLibrarySelector librarySelector,
            RuntimeEnvironment environment
    ) {
        List<RuntimeLibraryMetadata> libraries = manifestJson.libraries().stream()
                .map(LibraryEntryJson::toRuntimeLibraryMetadata)
                .toList();

        RuntimeLibrarySelection selection = librarySelector.select(libraries, environment);

        Manifest manifest = new Manifest(
                manifestJson.minecraftVersion(),
                manifestJson.loader().toLoaderInfo(),
                manifestJson.files().stream()
                        .map(FileEntryJson::toFileEntry)
                        .toList(),
                manifestJson.launchInfo().toLaunchInfo(),
                selection.selectedArtifacts()
        );

        return new ManifestLoadResult(
                manifest,
                selection
        );
    }

}
