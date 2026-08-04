package com.launcher.core.download;

import com.launcher.model.manifest.FileEntry;

import java.util.List;

public record DownloadPlan(
        List<FileEntry> files
) {

    public boolean isEmpty() {
        return files.isEmpty();
    }

}
