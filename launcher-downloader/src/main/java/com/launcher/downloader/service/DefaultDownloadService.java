package com.launcher.downloader.service;

import com.launcher.core.download.DownloadPlan;
import com.launcher.core.download.DownloadService;
import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.downloader.download.FileDownloader;
import com.launcher.model.manifest.FileEntry;

import java.nio.file.Path;

public class DefaultDownloadService implements DownloadService {
    private final DirectoryProvider directoryProvider;
    private final FileDownloader fileDownloader;

    public DefaultDownloadService(DirectoryProvider directoryProvider, FileDownloader fileDownloader) {
        this.directoryProvider = directoryProvider;
        this.fileDownloader = fileDownloader;
    }

    @Override
    public void download(DownloadPlan plan) {
        Path gameDirectory = directoryProvider.directories().game();

        for (FileEntry file : plan.files()) {
            Path targetPath = gameDirectory.resolve(file.path());
            fileDownloader.download(file.url(), targetPath);
        }
    }
}
