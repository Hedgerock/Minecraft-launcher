package com.launcher.core.architecture.boundary;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

class LauncherCoreBoundaryTest {

    private static final Path PROJECT_ROOT = findProjectRoot();

    private static final Path CORE_MAIN_SOURCES = PROJECT_ROOT
            .resolve("launcher-core")
            .resolve("src")
            .resolve("main")
            .resolve("java");

    private static final List<String> FORBIDDEN_IMPORTS = List.of(
            "import com.launcher.api.",
            "import com.launcher.storage.file.LocalFileStorage;",
            "import com.launcher.storage.file.LocalFileMetadataReader;"
    );

    @Test
    void launcher_core_should_not_import_concrete_infrastructure_adapters() throws IOException {
        List<String> violations = findViolations();

        if (!violations.isEmpty()) {
            fail(String.join(System.lineSeparator(), violations));
        }
    }

    private static Path findProjectRoot() {
        Path currentPath = Path.of("").toAbsolutePath();

        while (currentPath != null) {
            boolean isCurrentPath =
                    Files.exists(currentPath.resolve("settings.gradle")) ||
                            Files.exists(currentPath.resolve("settings.gradle.kts"));

            if (isCurrentPath) {

                return currentPath;
            }

            currentPath = currentPath.getParent();
        }

        throw new IllegalStateException("Project root not found");
    }

    private List<String> findViolations() throws IOException {
        try (Stream<Path> paths = Files.walk(CORE_MAIN_SOURCES)) {
            return paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .flatMap(this::violationsIn)
                    .toList();
        }
    }

    private Stream<String> violationsIn(Path sourceFile) {
        try {
            String content = Files.readString(sourceFile);

            return FORBIDDEN_IMPORTS.stream()
                    .filter(content::contains)
                    .map(forbiddenImport ->
                            "%s contains forbidden import: %s".formatted(sourceFile, forbiddenImport));
        } catch (IOException exception) {
            String message = "%s could not be inspected: %s".formatted(sourceFile, exception.getMessage());
            return Stream.of(message);
        }
    }
}
