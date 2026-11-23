package com.launcher.downloader.download;

import com.launcher.downloader.exception.DownloadException;
import com.launcher.storage.resolver.LocalWriteTargetResolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public class DefaultFileDownloader implements FileDownloader {
    private final DownloadSource downloadSource;
    private final ResourceDownloadLimits limits;
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
        this(
                downloadSource,
                writeTargetResolver,
                ResourceDownloadLimits.defaults()
        );
    }

    DefaultFileDownloader(
            DownloadSource downloadSource,
            LocalWriteTargetResolver writeTargetResolver,
            ResourceDownloadLimits limits
    ) {
        this.downloadSource = Objects.requireNonNull(downloadSource, "downloadSource");
        this.writeTargetResolver = Objects.requireNonNull(writeTargetResolver, "writeTargetResolver");
        this.limits = Objects.requireNonNull(limits, "limits");
    }

    public static FileDownloader forManagedResources(LocalWriteTargetResolver writeTargetResolver) {
        return new DefaultFileDownloader(
                new ManagedHttpDownloadSource(),
                writeTargetResolver
        );
    }

    @Override
    public void download(
            String url,
            Path trustedRoot,
            Path targetPath,
            long expectedSize
    ) {
        Path temporaryFile = null;

        try {
            limits.validateExpectedSize(expectedSize);

            Path preparedTargetPath = writeTargetResolver.prepareFileTarget(
                    trustedRoot,
                    targetPath
            );

            temporaryFile = Files.createTempFile(
                    preparedTargetPath.getParent(),
                    preparedTargetPath.getFileName().toString(),
                    ".download"
            );

            copyTempFile(url, temporaryFile, targetPath, expectedSize);

            if (Files.size(temporaryFile) != expectedSize) {
                throw DownloadException.sizeMismatch(url, targetPath);
            }

            safeMove(temporaryFile, preparedTargetPath);
        } catch (DownloadException failure) {
            if (temporaryFile != null) {
                deleteTemporaryFileAfterFailure(temporaryFile, failure);
            }

            throw failure;
        } catch (IOException | IllegalArgumentException failure) {
            if (temporaryFile != null) {
                deleteTemporaryFileAfterFailure(temporaryFile, failure);
            }

            throw DownloadException.downloadFailed(url, targetPath, failure);
        }
    }

    private void copyTempFile(String url, Path temporaryFile, Path targetPath, long expectedSize) throws IOException {
        byte[] buffer = new byte[8192];
        long writtenBytes = 0;

        try (
                InputStream input = downloadSource.open(url);
                OutputStream output = Files.newOutputStream(
                        temporaryFile,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING
                )
        ) {
            while (true) {
                long remainingBytes = expectedSize - writtenBytes;

                int readLimit = remainingBytes >= buffer.length
                        ? buffer.length
                        : (int) remainingBytes + 1;

                int count = input.read(buffer, 0, readLimit);

                if (count == -1) {
                    break;
                }

                if (count > remainingBytes) {
                    throw DownloadException.sizeMismatch(url, targetPath);
                }

                output.write(buffer, 0, count);
                writtenBytes += count;
            }

            if (writtenBytes != expectedSize) {
                throw DownloadException.sizeMismatch(url, targetPath);
            }
        }
    }

    private void deleteTemporaryFileAfterFailure(
            Path temporaryFile,
            Throwable downloadFailure
    ) {
        try {
            Files.deleteIfExists(temporaryFile);
        } catch (IOException cleanupFailure) {
            if (cleanupFailure != downloadFailure) {
                downloadFailure.addSuppressed(cleanupFailure);
            }
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
