package com.launcher.model.manifest;

import java.util.List;
import java.util.Objects;

public record Manifest(
        String minecraftVersion,
        LoaderInfo loader,
        List<FileEntry> files,
        LaunchInfo launchInfo
) {

    public Manifest {
        Objects.requireNonNull(files, "files");

        files = List.copyOf(files);
    }

}
