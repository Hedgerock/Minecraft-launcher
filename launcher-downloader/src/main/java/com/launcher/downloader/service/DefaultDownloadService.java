package com.launcher.downloader.service;

import com.launcher.core.download.DownloadService;
import com.launcher.core.download.model.DownloadPlan;
import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.downloader.download.FileDownloader;
import com.launcher.downloader.exception.DownloadException;
import com.launcher.model.manifest.ResourceEntry;

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

        for (ResourceEntry resource : plan.resources()) {
            Path targetPath = gameDirectory.resolve(resource.path());

            try {
                fileDownloader.download(resource.url(), targetPath);
            } catch (DownloadException exception) {
                throw exception.withPath(resource.path());
            }

            validateResourceSize(resource, targetPath);
        }
    }

    private void validateResourceSize(ResourceEntry resource, Path targetPath) {

        try {
            long actualSize = Files.size(targetPath);

            if (actualSize != resource.size()) {
                throw DownloadException
                        .sizeMismatch(resource.url(), resource.path(), targetPath);
            }
        } catch (IOException e) {
            throw DownloadException.sizeReadFailed(resource.url(), resource.path(), targetPath, e);
        }

    }
}
