package com.launcher.ui.architecture.boundary;

import com.launcher.testing.architecture.JavaSourceImportInspector;
import com.launcher.testing.architecture.ProjectRootLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class LauncherUiBoundaryTest {

    private static final Path PROJECT_ROOT = new ProjectRootLocator().findFrom(
            Path.of("").toAbsolutePath()
    );

    private static final Path UI_MAIN_SOURCES = PROJECT_ROOT
            .resolve("launcher-ui")
            .resolve("src")
            .resolve("main")
            .resolve("java");

    private final UiImportBoundaryCheck importBoundaryCheck = new UiImportBoundaryCheck();
    private final JavaSourceImportInspector sourceInspector = new JavaSourceImportInspector();

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
        List<String> result = sourceInspector.inspectFile(
                sourceFile,
                importBoundaryCheck::findForbiddenImports
        );

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
        List<String> result = sourceInspector.inspectFile(
                sourceFile,
                importBoundaryCheck::findForbiddenImports
        );

        //then
        assertEquals(List.of(), result);
    }

    private List<String> findViolations() throws IOException {
        return sourceInspector.inspectDirectory(
                UI_MAIN_SOURCES,
                importBoundaryCheck::findForbiddenImports
        );
    }
}
