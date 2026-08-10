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

    public String getUrl() {
        return url;
    }
}
