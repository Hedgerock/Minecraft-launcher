package com.launcher.downloader.download;

import com.launcher.downloader.exception.DownloadException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class DefaultFileDownloader implements FileDownloader {
    private final DownloadSource downloadSource;

    public DefaultFileDownloader() {
        this(url -> URI.create(url).toURL().openStream());
    }

    DefaultFileDownloader(DownloadSource downloadSource) {
        this.downloadSource = downloadSource;
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void download(String url, Path targetPath) {
        Path temporaryFile = null;

        try {
            Path parent = targetPath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            temporaryFile = Files.createTempFile(
                    parent,
                    targetPath.getFileName().toString(),
                    ".download"
            );

            copyTempFile(url, temporaryFile);

            safeMove(temporaryFile, targetPath);

        } catch (IOException | IllegalArgumentException e) {

            if (temporaryFile != null) {
                deleteTemporaryFileQuietly(temporaryFile);
            }

            throw DownloadException.downloadFailed(url, targetPath, e);
        }
    }

    private void copyTempFile(String url, Path temporaryFile) throws IOException {
        try(InputStream inputStream = downloadSource.open(url)) {
            Files.copy(
                    inputStream,
                    temporaryFile,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    private void deleteTemporaryFileQuietly(Path temporaryFile) {
        try {
            Files.deleteIfExists(temporaryFile);
        } catch (IOException ignored) {}
    }

    private void safeMove(Path source, Path targetPath) throws IOException {
        try {
            Files.move(
                    source,
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );

        } catch (AtomicMoveNotSupportedException e) {
            Files.move(
                    source,
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

}