package com.launcher.core.architecture.support.recording;

import com.launcher.core.storage.file.FileStorage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class RecordingFileStorage implements FileStorage {
    private final List<Path> createdDirectories = new ArrayList<>();

    @Override
    public void createDirectories(Path path) {
        createdDirectories.add(path);
    }

    @Override
    public void delete(Path path) {

    }

    public List<Path> getCreatedDirectories() {
        return List.copyOf(createdDirectories);
    }
}
