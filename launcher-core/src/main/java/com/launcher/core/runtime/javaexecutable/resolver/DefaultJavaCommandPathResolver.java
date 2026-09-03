package com.launcher.core.runtime.javaexecutable.resolver;

import com.launcher.core.runtime.javaexecutable.exception.JavaCommandPathResolutionException;
import com.launcher.core.runtime.javaexecutable.resolver.model.JavaCommandPathEnvironment;
import com.launcher.model.runtime.JavaExecutableReference;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public final class DefaultJavaCommandPathResolver implements JavaCommandPathResolver {
    private final JavaCommandPathEnvironment javaCommandPathEnvironment;

    public DefaultJavaCommandPathResolver(JavaCommandPathEnvironment javaCommandPathEnvironment) {
        this.javaCommandPathEnvironment =
                Objects.requireNonNull(javaCommandPathEnvironment, "javaCommandPathEnvironment");
    }

    @Override
    public JavaExecutableReference resolve(JavaExecutableReference javaExecutableReference) {
        Objects.requireNonNull(javaExecutableReference, "javaExecutableReference");

        if (javaExecutableReference.isExplicitPath()) {
            return javaExecutableReference;
        }

        String commandName = javaExecutableReference.value();

        Path resolvedPath = findExactMatch(commandName)
                .or(() -> findWithExtensions(commandName))
                .orElseThrow(() ->
                        new JavaCommandPathResolutionException(
                                "Java command not found: " + commandName
                        )
                );

        return JavaExecutableReference.explicitPath(resolvedPath.toString());
    }


    private Optional<Path> findExactMatch(String commandName) {
        return javaCommandPathEnvironment.directories().stream()
                .map(directory -> directory.resolve(commandName))
                .filter(Files::isRegularFile)
                .findFirst();
    }

    private Optional<Path> findWithExtensions(String commandName) {
        for (Path directory : javaCommandPathEnvironment.directories()) {
            for (String extension : javaCommandPathEnvironment.executableExtensions()) {
                Path candidate = directory.resolve(commandName + extension);

                if (Files.isRegularFile(candidate)) {
                    return Optional.of(candidate);
                }
            }
        }

        return Optional.empty();
    }
}
