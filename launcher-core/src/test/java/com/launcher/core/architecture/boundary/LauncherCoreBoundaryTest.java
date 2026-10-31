package com.launcher.core.architecture.boundary;

import com.launcher.testing.architecture.JavaSourceImportInspector;
import com.launcher.testing.architecture.ProjectRootLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class LauncherCoreBoundaryTest {

    private static final Path PROJECT_ROOT = new ProjectRootLocator()
            .findFrom(Path.of("").toAbsolutePath());

    private static final Path CORE_MAIN_SOURCES = PROJECT_ROOT
            .resolve("launcher-core")
            .resolve("src")
            .resolve("main")
            .resolve("java");

    private static final List<String> FORBIDDEN_CORE_SOURCE_FILES = List.of(
            "LocalFileStorage.java",
            "LocalDirectoryProvider.java",
            "SystemRuntimeEnvironmentProvider.java",
            "SystemJavaCommandPathEnvironmentProvider.java",
            "PathParser.java",
            "DefaultJavaExecutableReadinessChecker.java"
    );

    private final CoreImportBoundaryCheck importBoundaryCheck = new CoreImportBoundaryCheck();
    private final JavaSourceImportInspector sourceInspector = new JavaSourceImportInspector();

    @Test
    void should_not_contain_forbidden_concrete_adapters() throws IOException {
        List<String> violations = findForbiddenCoreClasses();

        if (!violations.isEmpty()) {
            fail(String.join(System.lineSeparator(), violations));
        }
    }

    @Test
    void should_report_formatted_forbidden_import_from_source_file(
            @TempDir Path tempDir
    ) throws IOException {
        //given
        Path sourceFile = tempDir.resolve("Example.java");

        Files.writeString(sourceFile, """
            import   static   com.launcher.app.Adapter.VALUE;
            class Example {}
            """);

        //when
        List<String> result = sourceInspector.inspectFile(
                sourceFile,
                importBoundaryCheck::findForbiddenImports
        );

        //then
        String expectedViolation = "%s contains forbidden import: %s".formatted(
                sourceFile,
                "import static com.launcher.app.Adapter.VALUE;"
        );

        assertEquals(List.of(expectedViolation), result);
    }

    private List<String> findForbiddenCoreClasses() throws IOException {
        try (Stream<Path> paths = Files.walk(CORE_MAIN_SOURCES)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> FORBIDDEN_CORE_SOURCE_FILES.contains(path.getFileName().toString()))
                    .map("%s is a concrete adapter and must not be located in launcher-core"::formatted)
                    .toList();
        }
    }

    @Test
    void should_not_import_concrete_infrastructure_adapters() throws IOException {
        List<String> violations = findViolations();

        if (!violations.isEmpty()) {
            fail(String.join(System.lineSeparator(), violations));
        }
    }

    private List<String> findViolations() throws IOException {
        return sourceInspector.inspectDirectory(
                CORE_MAIN_SOURCES,
                importBoundaryCheck::findForbiddenImports
        );
    }
}
