package com.launcher.downloader.download;

import com.launcher.downloader.exception.DownloadException;
import com.launcher.storage.resolver.LocalWriteTargetResolver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class DefaultFileDownloader implements FileDownloader {
    private final DownloadSource downloadSource;
    private final LocalWriteTargetResolver writeTargetResolver;

    public DefaultFileDownloader(LocalWriteTargetResolver writeTargetResolver) {
        this(
                url -> URI.create(url).toURL().openStream(),
                    writeTargetResolver
        );
    }

    DefaultFileDownloader(
            DownloadSource downloadSource,
            LocalWriteTargetResolver writeTargetResolver
    ) {
        this.downloadSource = Objects.requireNonNull(downloadSource, "downloadSource");
        this.writeTargetResolver = Objects.requireNonNull(writeTargetResolver, "writeTargetResolver");
    }

    public static FileDownloader forManagedResources(LocalWriteTargetResolver writeTargetResolver) {
        return new DefaultFileDownloader(
                new ManagedHttpDownloadSource(),
                writeTargetResolver
        );
    }

    @Override
    public void download(String url, Path trustedRoot, Path targetPath) {
        Path temporaryFile = null;

        try {
            Path preparedTargetPath = writeTargetResolver.prepareFileTarget(
                    trustedRoot,
                    targetPath
            );

            Path parent = preparedTargetPath.getParent();

            temporaryFile = Files.createTempFile(
                    parent,
                    preparedTargetPath.getFileName().toString(),
                    ".download"
            );

            copyTempFile(url, temporaryFile);
            safeMove(temporaryFile, preparedTargetPath);

        } catch (IOException | IllegalArgumentException e) {

            if (temporaryFile != null) {
                deleteTemporaryFileQuietly(temporaryFile);
            }

            throw DownloadException.downloadFailed(url, targetPath, e);
        }
    }

    private void copyTempFile(String url, Path temporaryFile) throws IOException {
        try (InputStream inputStream = downloadSource.open(url)) {
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
        } catch (IOException ignored) {
            // Best-effort cleanup: the original download failure must remain the reported cause
        }
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
