package com.launcher.storage.hash;

import java.nio.file.Path;

public interface HashService {

    String sha256(Path filePath);

}
