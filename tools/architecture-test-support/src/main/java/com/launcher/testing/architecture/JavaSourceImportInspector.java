package com.launcher.testing.architecture;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class JavaSourceImportInspector {

    public List<String> inspectFile(
            Path sourceFile,
            JavaSourceImportCheck check
    ) {
        Objects.requireNonNull(sourceFile, "sourceFile");
        Objects.requireNonNull(check, "check");

        try {
            String source = Files.readString(sourceFile);

            return check.findForbiddenImports(source).stream()
                    .map(declaration ->
                            "%s contains forbidden import: %s".formatted(sourceFile, declaration))
                    .toList();
        } catch (IOException | IllegalArgumentException e) {
            return List.of(
                    "%s could not be inspected: %s".formatted(sourceFile, e.getMessage())
            );
        }
    }

    public List<String> inspectDirectory(
            Path sourceDirectory,
            JavaSourceImportCheck check
    ) throws IOException {
        Objects.requireNonNull(sourceDirectory, "sourceDirectory");
        Objects.requireNonNull(check, "check");

        BasicFileAttributes attributes = Files.readAttributes(sourceDirectory, BasicFileAttributes.class);

        if (!attributes.isDirectory()) {
            throw new NotDirectoryException(sourceDirectory.toString());
        }

        try (Stream<Path> paths = Files.walk(sourceDirectory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .sorted()
                    .flatMap(path -> inspectFile(path, check).stream())
                    .toList();
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }
}
