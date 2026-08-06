package com.launcher.downloader.download;

import java.nio.file.Path;

public class DefaultFileDownloader implements FileDownloader {

    @Override
    public void download(String url, Path targetPath) {
        throw new UnsupportedOperationException("File download is not implemented yet");
    }
}
