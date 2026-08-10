package com.launcher.downloader.support;

import com.launcher.downloader.download.FileDownloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class RecordingFileDownloader implements FileDownloader {
    private final boolean withException;
    private boolean createFile;
    private final long fileSize;

    private final List<DownloadRequest> requests = new ArrayList<>();

    public RecordingFileDownloader() {
        this(false, true, 100L);
    }

    public RecordingFileDownloader(boolean withException) {
        this(withException, true, 100L);
    }

    public void setCreateFile(boolean createFile) {
        this.createFile = createFile;
    }

    public RecordingFileDownloader(boolean withException, boolean createFile, long fileSize) {
        this.withException = withException;
        this.createFile = createFile;
        this.fileSize = fileSize;
    }

    public record DownloadRequest(String url, Path targetPath) {}

    @Override
    public void download(String url, Path targetPath) {
        if (withException) {
            throw new RuntimeException("Download failed");
        }

        requests.add(new DownloadRequest(url, targetPath));

        if (createFile) {
            createFile(targetPath);
        }
    }

    private void createFile(Path targetPath) {
        try {
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, new byte[(int) fileSize]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<DownloadRequest> getRequests() {
        return requests;
    }
}
