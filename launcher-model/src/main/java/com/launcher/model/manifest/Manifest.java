package com.launcher.model.manifest;

import java.util.List;
import java.util.Objects;

public record Manifest(
        String minecraftVersion,
        LoaderInfo loader,
        List<FileEntry> files,
        LaunchInfo launchInfo,
        List<LibraryEntry> libraries
) {

    public Manifest {
        Objects.requireNonNull(files, "files");
        Objects.requireNonNull(libraries, "libraries");

        files = List.copyOf(files);
        libraries = List.copyOf(libraries);
    }

}
