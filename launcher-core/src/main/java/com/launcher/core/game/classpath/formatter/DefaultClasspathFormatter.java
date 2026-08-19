package com.launcher.core.game.classpath.formatter;

import com.launcher.core.game.classpath.GameClasspath;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Collectors;

public class DefaultClasspathFormatter implements ClasspathFormatter {

    @Override
    public String format(GameClasspath classpath) {
        Objects.requireNonNull(classpath, "classpath");

        return classpath.entries().stream()
                .map(Path::toString)
                .collect(Collectors.joining(File.pathSeparator));
    }
}
