package com.launcher.core.runtime.javaexecutable.resolver.model;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public record JavaCommandPathEnvironment(
        List<Path> directories,
        List<String> executableExtensions
) {

    public JavaCommandPathEnvironment {
        Objects.requireNonNull(directories, "directories");
        Objects.requireNonNull(executableExtensions, "executableExtensions");

        directories = List.copyOf(directories);
        executableExtensions = List.copyOf(executableExtensions);
    }

}
