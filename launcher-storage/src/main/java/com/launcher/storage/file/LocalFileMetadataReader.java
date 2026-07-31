package com.launcher.storage.file;

import com.launcher.core.storage.exception.StorageException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LocalFileMetadataReader implements FileMetadataReader {

    @Override
    public boolean exists(Path path) {
        return Files.exists(path);
    }

    @Override
    public long size(Path path) {

        try {
            return Files.size(path);
        } catch (IOException exception) {
            throw new StorageException(
                    "Unable to read file size",
                    exception
            );
        }

    }
}
