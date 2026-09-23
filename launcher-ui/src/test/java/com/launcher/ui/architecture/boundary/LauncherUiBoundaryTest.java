package com.launcher.ui.architecture.boundary;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

class LauncherUiBoundaryTest {
    private static final Path PROJECT_ROOT = findProjectRoot();

    private static final String CORE_IMPORT_PREFIX = "com.launcher.core.";
    private static final String APPLICATION_IMPORT_PREFIX = "com.launcher.app.";

    private static final Path UI_MAIN_SOURCES = PROJECT_ROOT
            .resolve("launcher-ui")
            .resolve("src")
            .resolve("main")
            .resolve("java");

    private static final Set<String> ALLOWED_CORE_IMPORTS = Set.of(
            "import com.launcher.core.LaunchResult;",
            "import com.launcher.core.LaunchFailure;",
            "import com.launcher.core.configuration.LauncherConfiguration;",
            "import com.launcher.core.operation.type.OperationType;"
    );

    private static final Set<String> ALLOWED_APPLICATION_IMPORTS = Set.of(
            "import com.launcher.app.bootstrap.Bootstrap;",
            "import com.launcher.app.configuration.LauncherConfigurationResolver;",
            "import com.launcher.app.presentation.LaunchRequestResult;",
            "import com.launcher.app.presentation.PresentationLaunchBoundary;",
            "import com.launcher.app.result.LauncherResultHandler;"
    );

    @Test
    void should_reject_internal_application_imports_in_launcher_ui() throws IOException {
        List<String> violations = findViolations(
                APPLICATION_IMPORT_PREFIX,
                ALLOWED_APPLICATION_IMPORTS
        );

        if (!violations.isEmpty()) {
            fail(String.join(System.lineSeparator(), violations));
        }
    }

    @Test
    void should_reject_forbidden_core_imports_in_launcher_ui() throws IOException {
        List<String> violations = findViolations(CORE_IMPORT_PREFIX, ALLOWED_CORE_IMPORTS);

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

    private List<String> findViolations(String importPrefix, Set<String> allowedImports) throws IOException {
        try (Stream<Path> paths = Files.walk(UI_MAIN_SOURCES)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .flatMap(path -> violationsIn(path, importPrefix, allowedImports))
                    .toList();
        }
    }

    private Stream<String> violationsIn(Path sourceFile, String importPrefix, Set<String> allowedImports) {
        try {
            String content = Files.readString(sourceFile);

            return content.lines()
                    .map(String::trim)
                    .filter(line -> isImportFrom(line, importPrefix))
                    .filter(line -> !allowedImports.contains(line))
                    .map(importLine ->
                            "%s contains forbidden import: %s".formatted(sourceFile, importLine));
        } catch (IOException exception) {
            String message = "%s could not be inspected: %s".formatted(sourceFile, exception.getMessage());

            return Stream.of(message);
        }
    }

    private boolean isImportFrom(
            String line,
            String importPrefix
    ) {
        return line.startsWith("import " + importPrefix) ||
                line.startsWith("import static " + importPrefix);
    }
}
