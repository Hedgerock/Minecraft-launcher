package com.launcher.app.architecture.boundary;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class LauncherAppBoundaryTest {

    private static final Path PROJECT_ROOT = findProjectRoot();

    private static final Path APP_MAIN_SOURCES = PROJECT_ROOT
            .resolve("launcher-app")
            .resolve("src")
            .resolve("main")
            .resolve("java");

    private final AppImportBoundaryCheck importBoundaryCheck =
            new AppImportBoundaryCheck();

    @Test
    void should_not_import_ui_or_javafx_types() throws IOException {
        //when
        List<String> violations = findViolations();

        //then
        if (!violations.isEmpty()) {
            fail(String.join(System.lineSeparator(), violations));
        }
    }

    @Test
    void should_report_formatted_forbidden_imports_from_source_file(
            @TempDir Path tempDir
    ) throws IOException {
        //given
        Path sourceFile = tempDir.resolve("Example.java");

        Files.writeString(sourceFile, """
                import
                    com.launcher.ui.Adapter;
                import   static   javafx.application.Platform.runLater;
                import /* internal */ com.sun.javafx.application.PlatformImpl;
                class Example {}
                """);

        //when
        List<String> result = violationsIn(sourceFile).toList();

        //then
        assertEquals(
                List.of(
                        "%s contains forbidden import: %s".formatted(
                                sourceFile,
                                "import com.launcher.ui.Adapter;"
                        ),
                        "%s contains forbidden import: %s".formatted(
                                sourceFile,
                                "import static javafx.application.Platform.runLater;"
                        ),
                        "%s contains forbidden import: %s".formatted(
                                sourceFile,
                                "import com.sun.javafx.application.PlatformImpl;"
                        )
                ),
                result
        );
    }

    @Test
    void should_allow_application_and_adapter_imports_from_source_file(
            @TempDir Path tempDir
    ) throws IOException {
        //given
        Path sourceFile = tempDir.resolve("Example.java");

        Files.writeString(sourceFile, """
                import
                    com.launcher.core.LaunchResult;
                import   com.launcher.storage.file.LocalFileStorage;
                import com.launcher.model.manifest.Manifest;
                class Example {}
                """);

        //when
        List<String> result = violationsIn(sourceFile).toList();

        //then
        assertEquals(List.of(), result);
    }

    @Test
    void should_report_inspection_failure_when_source_file_cannot_be_read(
            @TempDir Path tempDir
    ) {
        //given
        Path sourceFile = tempDir.resolve("Missing.java");

        //when
        List<String> result = violationsIn(sourceFile).toList();

        //then
        assertEquals(1, result.size());
        assertTrue(
                result.getFirst().startsWith(
                        sourceFile + " could not be inspected: "
                )
        );
    }

    @Test
    void should_report_inspection_failure_when_source_file_is_invalid(
            @TempDir Path tempDir
    ) throws IOException {
        //given
        Path sourceFile = tempDir.resolve("Example.java");
        Files.writeString(sourceFile, "class Example {");

        //when
        List<String> result = violationsIn(sourceFile).toList();

        //then
        assertEquals(1, result.size());
        assertTrue(
                result.getFirst().startsWith(
                        sourceFile + " could not be inspected: Cannot parse Java source:"
                )
        );
    }

    private List<String> findViolations() throws IOException {
        try (Stream<Path> paths = Files.walk(APP_MAIN_SOURCES)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .sorted()
                    .flatMap(this::violationsIn)
                    .toList();
        }
    }

    private Stream<String> violationsIn(Path sourceFile) {
        try {
            String content = Files.readString(sourceFile);

            return importBoundaryCheck.findForbiddenImports(content).stream()
                    .map(forbiddenImport ->
                            "%s contains forbidden import: %s".formatted(
                                    sourceFile,
                                    forbiddenImport
                            )
                    );
        } catch (IOException | IllegalArgumentException exception) {
            return Stream.of(
                    "%s could not be inspected: %s".formatted(
                            sourceFile,
                            exception.getMessage()
                    )
            );
        }
    }

    private static Path findProjectRoot() {
        Path currentPath = Path.of("").toAbsolutePath();

        while (currentPath != null) {
            boolean isProjectRoot =
                    Files.exists(currentPath.resolve("settings.gradle")) ||
                            Files.exists(currentPath.resolve("settings.gradle.kts"));

            if (isProjectRoot) {
                return currentPath;
            }

            currentPath = currentPath.getParent();
        }

        throw new IllegalStateException("Project root not found");
    }
}
