package com.launcher.api.manifest.mapper.dto;

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

}
