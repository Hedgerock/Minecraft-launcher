package com.launcher.downloader.support;

import com.launcher.downloader.download.FileDownloader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class WritingFileDownloader implements FileDownloader {

    private final byte[] content;

    public WritingFileDownloader(String content) {
        this.content = content.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void download(String url, Path targetPath) {
        try {
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
