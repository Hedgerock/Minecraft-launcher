package com.launcher.ui.architecture.boundary;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class LauncherUiBoundaryTest {

    private static final Path PROJECT_ROOT = findProjectRoot();

    private static final Path UI_MAIN_SOURCES = PROJECT_ROOT
            .resolve("launcher-ui")
            .resolve("src")
            .resolve("main")
            .resolve("java");

    private final UiImportBoundaryCheck importBoundaryCheck = new UiImportBoundaryCheck();

    @Test
    void should_not_import_forbidden_application_or_core_types() throws IOException {
        List<String> violations = findViolations();

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
                    com.launcher.app.presentation.DefaultPresentationLaunchBoundary;
                import   static   com.launcher.core.LaunchResult.success;
                class Example {}
                """);

        //when
        List<String> result = violationsIn(sourceFile).toList();

        //then
        assertEquals(
                List.of(
                        "%s contains forbidden import: %s".formatted(
                                sourceFile,
                                "import com.launcher.app.presentation.DefaultPresentationLaunchBoundary;"
                        ),
                        "%s contains forbidden import: %s".formatted(
                                sourceFile,
                                "import static com.launcher.core.LaunchResult.success;"
                        )
                ),
                result
        );
    }

    @Test
    void should_allow_formatted_public_imports_from_source_file(
            @TempDir Path tempDir
    ) throws IOException {
        //given
        Path sourceFile = tempDir.resolve("Example.java");

        Files.writeString(sourceFile, """
                import   com.launcher.core.LaunchResult; // public result
                import
                    com.launcher.app.presentation.PresentationLaunchBoundary;
                class Example {}
                """);

        //when
        List<String> result = violationsIn(sourceFile).toList();

        //then
        assertEquals(List.of(), result);
    }

    private List<String> findViolations() throws IOException {
        try (Stream<Path> paths = Files.walk(UI_MAIN_SOURCES)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .flatMap(this::violationsIn)
                    .toList();
        }
    }

    private Stream<String> violationsIn(Path sourceFile) {
        try {
            String content = Files.readString(sourceFile);

            return importBoundaryCheck.findForbiddenImports(content).stream()
                    .map(forbiddenImport -> "%s contains forbidden import: %s".formatted(sourceFile, forbiddenImport));
        } catch (IOException | IllegalArgumentException e) {
            String message = "%s could not be inspected: %s".formatted(sourceFile, e.getMessage());

            return Stream.of(message);
        }
    }

    private static Path findProjectRoot() {
        Path currentPath = Path.of("").toAbsolutePath();

        while (currentPath != null) {
            boolean isCurrentPath = Files.exists(currentPath.resolve("settings.gradle")) ||
                    Files.exists(currentPath.resolve("settings.gradle.kts"));

            if (isCurrentPath) {
                return currentPath;
            }

            currentPath = currentPath.getParent();
        }

        throw new IllegalStateException("Project root not found");
    }
}
