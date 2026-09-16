package com.launcher.api.manifest.mapper.dto;

import java.util.List;
import java.util.Objects;

public record ManifestJson(
        String minecraftVersion,
        LoaderJson loader,
        List<FileEntryJson> files,
        LaunchInfoJson launchInfo,
        List<LibraryEntryJson> libraries,
        List<AssetEntryJson> assets
) {

    public ManifestJson(
            String minecraftVersion,
            LoaderJson loader,
            List<FileEntryJson> files,
            LaunchInfoJson launchInfo,
            List<LibraryEntryJson> libraries
    ) {
        this(
                minecraftVersion,
                loader,
                files,
                launchInfo,
                libraries,
                List.of()
        );
    }

    public ManifestJson {
        Objects.requireNonNull(loader, "loader");
        Objects.requireNonNull(files, "files");
        Objects.requireNonNull(launchInfo, "launchInfo");
        Objects.requireNonNull(libraries, "libraries");
        assets = Objects.requireNonNullElseGet(
                assets,
                List::of
        );
    }

}
