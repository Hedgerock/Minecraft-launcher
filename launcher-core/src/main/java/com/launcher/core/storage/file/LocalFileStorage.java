package com.launcher.core.storage.file;

import com.launcher.core.storage.exception.StorageException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LocalFileStorage implements FileStorage {

    @Override
    public void createDirectories(Path path) {

        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new StorageException(
                    "Unable to create directory",
                    e
            );
        }

    }

    @Override
    public void delete(Path path) {

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new StorageException(
                    "Failed to delete files",
                    e
            );
        }

    }
}
