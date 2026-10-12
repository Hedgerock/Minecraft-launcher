package com.launcher.api.manifest.mapper.dto;

import java.util.List;
import java.util.Objects;

public record ManifestJson(
        String minecraftVersion,
        LoaderJson loader,
        List<FileEntryJson> files,
        LaunchInfoJson launchInfo,
        List<LibraryEntryJson> libraries,
        List<AssetEntryJson> assets,
        AssetIndexMetadataJson assetIndex
) {

    public ManifestJson(
            String minecraftVersion,
            LoaderJson loader,
            List<FileEntryJson> files,
            LaunchInfoJson launchInfo,
            List<LibraryEntryJson> libraries,
            List<AssetEntryJson> assets
    ) {
        this(
                minecraftVersion,
                loader,
                files,
                launchInfo,
                libraries,
                assets,
                null
        );
    }

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
                List.of(),
                null
        );
    }

    public ManifestJson {
        Objects.requireNonNull(files, "files");
        Objects.requireNonNull(launchInfo, "launchInfo");
        Objects.requireNonNull(libraries, "libraries");
        assets = Objects.requireNonNullElseGet(
                assets,
                List::of
        );
    }

}
