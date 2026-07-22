package com.launcher.model.manifest;

import java.util.List;

public record Manifest(
        String minecraftVersion,
        LoaderInfo loader,
        List<FileEntry> files
) {
}
