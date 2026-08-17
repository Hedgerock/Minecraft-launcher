package com.launcher.downloader.service;

import com.launcher.core.download.model.DownloadPlan;
import com.launcher.core.download.DownloadService;
import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.downloader.download.FileDownloader;
import com.launcher.downloader.exception.DownloadException;
import com.launcher.model.manifest.FileEntry;

import java.io.IOException;
import java.nio.file.Files;
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

            validateFileSize(file, targetPath);
        }
    }

    private void validateFileSize(FileEntry file, Path targetPath) {

        try {
            long actualSize = Files.size(targetPath);

            if (actualSize != file.size()) {
                throw DownloadException.sizeMismatch(file.url(), file.path());
            }
        } catch (IOException e) {
            throw DownloadException.fileSizeReadFailed(file.url(), file.path(), e);
        }

    }
}
