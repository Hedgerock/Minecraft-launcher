package com.launcher.core.runtime.javaexecutable.resolver.provider;

import com.launcher.core.runtime.javaexecutable.resolver.model.JavaCommandPathEnvironment;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public final class SystemJavaCommandPathEnvironmentProvider implements JavaCommandPathEnvironmentProvider {
    private final Function<String, String> environmentVariableProvider;
    private final PathParser pathParser;

    public SystemJavaCommandPathEnvironmentProvider() {
        this(System::getenv, Path::of);
    }

    SystemJavaCommandPathEnvironmentProvider(
            Function<String, String> environmentVariableProvider,
            PathParser pathParser
    ) {
        this.environmentVariableProvider = Objects.requireNonNull(
                environmentVariableProvider,
                "environmentVariableProvider"
        );

        this.pathParser = Objects.requireNonNull(pathParser, "pathParser");
    }

    @Override
    public JavaCommandPathEnvironment current() {
        String path = environmentVariableProvider.apply("PATH");
        String pathExtensions = environmentVariableProvider.apply("PATHEXT");

        List<Path> directories = path == null
                ? List.of()
                : getDirectories(path);

        List<String> extensionsList = pathExtensions == null
                ? List.of()
                : getExtensions(pathExtensions);

        return new JavaCommandPathEnvironment(
                directories,
                extensionsList
        );
    }

    private List<Path> getDirectories(String path) {
        return Arrays.stream(path.split(File.pathSeparator))
                .filter(entry -> !entry.isBlank())
                .map(this::toPath)
                .flatMap(Optional::stream)
                .toList();
    }

    private List<String> getExtensions(String pathExtensions) {
        return Arrays.stream(pathExtensions.split(File.pathSeparator))
                .filter(extension -> !extension.isBlank())
                .map(extension -> extension.toLowerCase(Locale.ROOT))
                .toList();
    }

    private Optional<Path> toPath(String entry) {
        try {
            return Optional.of(
                    pathParser.parse(entry)
            );
        } catch (InvalidPathException e) {
            return Optional.empty();
        }
    }
}
