package com.launcher.downloader.exception;

public class DownloadException extends RuntimeException {

    private final String url;


    public DownloadException(String url, Throwable cause) {
        super("Failed to download file: " + url, cause);
        this.url = url;
    }

    public DownloadException(String message, String url) {
        super(message);
        this.url = url;
    }

    public DownloadException(String message, String url, Throwable cause) {
        super(message, cause);
        this.url = url;
    }

    public static DownloadException sizeMismatch(String url, String path) {
        return new DownloadException(
                "Downloaded file size mismatch: " + path,
                url
        );
    }

    public static DownloadException fileSizeReadFailed(String url, String path, Throwable cause) {
        return new DownloadException(
                "Failed to get file size: " + path,
                url,
                cause
        );
    }


    public String getUrl() {
        return url;
    }
}
