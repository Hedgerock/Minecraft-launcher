package com.launcher.core.download.model;

import com.launcher.model.manifest.FileEntry;

import java.util.List;
import java.util.Objects;

public record DownloadPlan(
        List<FileEntry> files
) {

    public DownloadPlan {
        Objects.requireNonNull(files, "files");

        files = List.copyOf(files);
    }
    public boolean isEmpty() {
        return files.isEmpty();
    }

}
