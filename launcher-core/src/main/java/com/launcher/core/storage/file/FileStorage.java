package com.launcher.core.storage.file;

import java.nio.file.Path;

public interface FileStorage {
    void createDirectories(Path path);

    void delete(Path path);

}
