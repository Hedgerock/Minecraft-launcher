package com.launcher.downloader.download;

import com.launcher.downloader.exception.DownloadException;
import com.launcher.storage.resolver.LocalWriteTargetResolver;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class DefaultFileDownloader implements FileDownloader {
    private final ResourceTransfer resourceTransfer;
    private final ResourceDownloadLimits limits;
    private final LocalWriteTargetResolver writeTargetResolver;

    public DefaultFileDownloader(LocalWriteTargetResolver writeTargetResolver) {
        this(
                new TechnicalResourceTransfer(new HttpResourceTransfer()),
                writeTargetResolver,
                ResourceDownloadLimits.defaults()
        );
    }

    DefaultFileDownloader(
            DownloadSource source,
            LocalWriteTargetResolver writeTargetResolver
    ) {
        this(source, writeTargetResolver, ResourceDownloadLimits.defaults());
    }

    DefaultFileDownloader(
            DownloadSource source,
            LocalWriteTargetResolver writeTargetResolver,
            ResourceDownloadLimits limits
    ) {
        this(new StreamResourceTransfer(source), writeTargetResolver, limits);
    }

    DefaultFileDownloader(
            ResourceTransfer resourceTransfer,
            LocalWriteTargetResolver writeTargetResolver,
            ResourceDownloadLimits limits
    ) {
        this.resourceTransfer = Objects.requireNonNull(resourceTransfer, "resourceTransfer");
        this.writeTargetResolver = Objects.requireNonNull(writeTargetResolver, "writeTargetResolver");
        this.limits = Objects.requireNonNull(limits, "limits");
    }

    public static FileDownloader forManagedResources(LocalWriteTargetResolver writeTargetResolver) {
        return new DefaultFileDownloader(
                new ManagedResourceTransfer(),
                writeTargetResolver,
                ResourceDownloadLimits.defaults()
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

            resourceTransfer.transfer(url, temporaryFile, targetPath, expectedSize, limits);

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
