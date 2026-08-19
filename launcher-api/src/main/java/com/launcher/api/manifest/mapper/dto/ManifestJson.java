package com.launcher.api.manifest.mapper.dto;

import com.launcher.model.manifest.Manifest;

import java.util.List;
import java.util.Objects;

public record ManifestJson(
        String minecraftVersion,
        LoaderJson loader,
        List<FileEntryJson> files,
        LaunchInfoJson launchInfo,
        List<LibraryEntryJson> libraries
) {

    public ManifestJson {
        Objects.requireNonNull(loader, "loader");
        Objects.requireNonNull(files, "files");
        Objects.requireNonNull(launchInfo, "launchInfo");
        Objects.requireNonNull(libraries, "libraries");
    }

    public Manifest toManifest() {
        return new Manifest(
                minecraftVersion,
                loader.toLoaderInfo(),
                files.stream().map(FileEntryJson::toFileEntry).toList(),
                launchInfo.toLaunchInfo(),
                libraries.stream().map(LibraryEntryJson::toLibraryEntry).toList()
        );
    }

}
