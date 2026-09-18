package com.launcher.downloader.service;

import com.launcher.core.download.DownloadService;
import com.launcher.core.download.model.DownloadPlan;
import com.launcher.core.resource.ResourceSetPlanner;
import com.launcher.core.resource.model.PlannedResource;
import com.launcher.core.resource.model.ResourceSetPlan;
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
    private final ResourceSetPlanner resourceSetPlanner;

    public DefaultDownloadService(
            DirectoryProvider directoryProvider,
            FileDownloader fileDownloader,
            ResourceSetPlanner resourceSetPlanner
    ) {
        this.directoryProvider = directoryProvider;
        this.fileDownloader = fileDownloader;
        this.resourceSetPlanner = resourceSetPlanner;
    }

    @Override
    public void download(DownloadPlan plan) {
        ResourceSetPlan resourceSetPlan = resourceSetPlanner.plan(
                plan.resources(),
                directoryProvider.directories().game()
        );

        for (PlannedResource plannedResource : resourceSetPlan.resources()) {
            ResourceEntry resource = plannedResource.resource();
            Path targetPath = plannedResource.targetPath();

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
