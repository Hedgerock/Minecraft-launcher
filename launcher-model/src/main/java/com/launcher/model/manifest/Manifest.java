package com.launcher.model.manifest;

import com.launcher.model.manifest.assets.AssetsIndex;

import java.util.List;
import java.util.Objects;

public record Manifest(
        String minecraftVersion,
        LoaderInfo loader,
        List<FileEntry> files,
        LaunchInfo launchInfo,
        List<LibraryEntry> libraries,
        AssetsIndex assetsIndex
) {

    public Manifest(
            String minecraftVersion,
            LoaderInfo loaderInfo,
            List<FileEntry> files,
            LaunchInfo launchInfo,
            List<LibraryEntry> libraries
    ) {
        this(
                minecraftVersion,
                loaderInfo,
                files,
                launchInfo,
                libraries,
                null
        );
    }

    public Manifest {
        Objects.requireNonNull(files, "files");
        Objects.requireNonNull(libraries, "libraries");
        assetsIndex = Objects.requireNonNullElseGet(
                assetsIndex,
                () -> new AssetsIndex(List.of())
        );

        files = List.copyOf(files);
        libraries = List.copyOf(libraries);
    }

}
