package com.launcher.api.manifest.mapper.dto;

import com.launcher.api.manifest.library.RuntimeLibrarySelector;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.RuntimeLibraryMetadata;
import com.launcher.model.runtime.RuntimeEnvironment;

import java.util.List;

public final class ManifestJsonConverter {

    public Manifest toManifest(
            ManifestJson manifestJson,
            RuntimeLibrarySelector librarySelector,
            RuntimeEnvironment environment
    ) {
        List<RuntimeLibraryMetadata> libraries = manifestJson.libraries().stream()
                .map(LibraryEntryJson::toRuntimeLibraryMetadata)
                .toList();

        return new Manifest(
                manifestJson.minecraftVersion(),
                manifestJson.loader().toLoaderInfo(),
                manifestJson.files().stream()
                        .map(FileEntryJson::toFileEntry)
                        .toList(),
                manifestJson.launchInfo().toLaunchInfo(),
                librarySelector.select(libraries, environment)
        );
    }

}
