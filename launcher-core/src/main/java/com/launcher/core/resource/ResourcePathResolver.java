package com.launcher.core.resource;

import java.nio.file.Path;

public interface ResourcePathResolver {

    Path resolve(Path baseDirectory, String resourcePath);

}
