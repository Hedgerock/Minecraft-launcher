package com.launcher.downloader.support;

import com.launcher.downloader.download.FileDownloader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class RecordingFileDownloader implements FileDownloader {
    private boolean withException = false;
    private final List<DownloadRequest> requests = new ArrayList<>();

    public RecordingFileDownloader() {
    }

    public RecordingFileDownloader(boolean withException) {
        this.withException = withException;
    }

    public record DownloadRequest(String url, Path targetPath) {}

    @Override
    public void download(String url, Path targetPath) {
        if (withException) {
            throw new RuntimeException("Download failed");
        }

        requests.add(new DownloadRequest(url, targetPath));
    }

    public List<DownloadRequest> getRequests() {
        return requests;
    }
}
