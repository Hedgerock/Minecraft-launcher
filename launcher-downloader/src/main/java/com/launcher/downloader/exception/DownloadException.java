package com.launcher.downloader.exception;

import java.util.Objects;
import java.util.Optional;

public class DownloadException extends RuntimeException {

    private final String url;
    private final DownloadExceptionReason reason;
    private final String path;

    DownloadException(DownloadExceptionReason reason, String message, String url, String path, Throwable cause) {
        super(message, cause);
        this.reason = Objects.requireNonNull(reason, "reason");
        this.url = Objects.requireNonNull(url, "url");
        this.path = path;
    }

    public DownloadException(String url, Throwable cause) {
        this(
                DownloadExceptionReason.DOWNLOAD_FAILED,
                "Failed to download file: " + url,
                url,
                null,
                cause
        );
    }

    public static DownloadException sizeMismatch(String url, String path) {
        return new DownloadException(
                DownloadExceptionReason.SIZE_MISMATCH,
                "Downloaded file size mismatch: " + path,
                url,
                path,
                null
        );
    }

    public static DownloadException fileSizeReadFailed(String url, String path, Throwable cause) {
        return new DownloadException(
                DownloadExceptionReason.SIZE_READ_FAILED,
                "Failed to get file size: " + path,
                url,
                path,
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
}
