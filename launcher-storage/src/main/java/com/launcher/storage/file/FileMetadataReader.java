package com.launcher.storage.file;

import java.nio.file.Path;

public interface FileMetadataReader {

    boolean exists(Path path);

    long size(Path path);

}
