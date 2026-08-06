package com.launcher.downloader.download;

import java.nio.file.Path;

public interface FileDownloader {

    void download(String url, Path targetPath);

}
