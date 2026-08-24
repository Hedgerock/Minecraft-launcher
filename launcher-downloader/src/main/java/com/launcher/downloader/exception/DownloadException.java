package com.launcher.downloader.exception;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public class DownloadException extends RuntimeException {

    private final String url;
    private final DownloadExceptionReason reason;
    private final String path;
    private final Path targetPath;

    DownloadException(
            DownloadExceptionReason reason,
            String message,
            String url,
            String path,
            Throwable cause,
            Path targetPath
    ) {
        super(message, cause);
        this.reason = Objects.requireNonNull(reason, "reason");
        this.url = Objects.requireNonNull(url, "url");
        this.path = path;
        this.targetPath = targetPath;
    }

    public static DownloadException sizeMismatch(
            String url,
            String path,
            Path targetPath
    ) {
        return new DownloadException(
                DownloadExceptionReason.SIZE_MISMATCH,
                "Downloaded resource size mismatch: " + path,
                url,
                path,
                null,
                targetPath
        );
    }

    public DownloadException withPath(String path) {
        return new DownloadException(
                reason,
                getMessage(),
                url,
                path,
                getCause(),
                targetPath
        );
    }

    public static DownloadException sizeReadFailed(
            String url,
            String path,
            Path targetPath,
            Throwable cause
    ) {
        return new DownloadException(
                DownloadExceptionReason.SIZE_READ_FAILED,
                "Failed to get resource size: " + path,
                url,
                path,
                cause,
                targetPath
        );
    }

    public static DownloadException downloadFailed(
            String url,
            String path,
            Path targetPath,
            Throwable cause
    ) {
        String pathMessage =
                path != null
                        ? " (" + path + ")"
                        : "";

        String message =
                "Failed to download resource" +
                        pathMessage +
                        " from " +
                        url;

        return new DownloadException(
                DownloadExceptionReason.DOWNLOAD_FAILED,
                message,
                url,
                path,
                cause,
                targetPath
        );
    }

    public static DownloadException downloadFailed(
            String url,
            Path targetPath,
            Throwable cause
    ) {
        return downloadFailed(
                url,
                null,
                targetPath,
                cause
        );
    }


    public String getUrl() {
        return url;
    }

    public DownloadExceptionReason getReason() {
        return reason;
    }

    public Optional<String> getPath() {
        return Optional.ofNullable(path);
    }

    public Optional<Path> getTargetPath() {
        return Optional.ofNullable(targetPath);
    }
}
