package com.launcher.testing.architecture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class JavaSourceImportInspectorTest {

    @TempDir
    Path tempDir;

    private final JavaSourceImportInspector inspector = new JavaSourceImportInspector();

    @Test
    void should_return_empty_list_when_source_has_no_violations() throws IOException {
        //given
        Path sourceFile = writeSource("Example.java", "class Example {}");

        //when
        List<String> result = inspector.inspectFile(
                sourceFile,
                source -> List.of()
        );

        //then
        assertEquals(List.of(), result);
    }

    @Test
    void should_report_failure_without_invoking_check() {
        //given
        Path sourceFile = tempDir.resolve("Missing.java");

        //when
        List<String> result = inspector.inspectFile(
                sourceFile,
                source -> fail("Check must not be called when source cannot be read")
        );

        //then
        assertEquals(1, result.size());
        assertTrue(
                result.getFirst().startsWith(sourceFile + " could not be inspected: ")
        );
    }

    @Test
    void should_read_source_and_report_imports_in_given_order() throws IOException {
        //given
        String source = """
                class Example {
                    String value = "Hello world!";
                """;

        Path sourceFile = writeSource("Example.java", source);

        List<String> inspectedSources = new ArrayList<>();

        JavaSourceImportCheck check = content -> {
            inspectedSources.add(content);

            return List.of(
                    "import example.Second;",
                    "import static example.First.VALUE;"
            );
        };

        //when
        List<String> result = inspector.inspectFile(sourceFile, check);

        //then
        assertEquals(List.of(source), inspectedSources);

        assertEquals(
                List.of(
                        sourceFile + " contains forbidden import: import example.Second;",
                        sourceFile + " contains forbidden import: import static example.First.VALUE;"
                ),
                result
        );
    }

    @ParameterizedTest
    @MethodSource("failedChecks")
    void should_report_failure_when_source_check_cannot_complete(
            JavaSourceImportCheck check
    ) throws IOException {
        //given
        Path sourceFile = writeSource("Example.java", "class Example {}");

        //when
        List<String> result = inspector.inspectFile(sourceFile, check);

        //then
        assertEquals(
                List.of(
                        sourceFile + " could not be inspected: inspection failed"
                ),
                result
        );
    }

    @Test
    void should_not_hide_unexpected_check_failure() throws IOException {
        //given
        Path sourceFile = writeSource("Example.java", "class Example {}");

        IllegalStateException failure = new IllegalStateException(
                "unexpected failure"
        );

        JavaSourceImportCheck check = source -> {
            throw failure;
        };

        //when & then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> inspector.inspectFile(sourceFile, check)
        );

        assertSame(failure, exception);
    }

    @Test
    void should_inspect_only_regular_java_files_recursively() throws IOException {
        //given
        Path firstFile = writeSource("A.java", "class A {}");
        Path secondFile = writeSource("nested/B.java", "class B {}");

        writeSource("README.txt", "not a Java source");
        writeSource("Ignored.java/data.txt", "not a Java source");

        //when
        List<String> result = inspector.inspectDirectory(
                tempDir,
                source -> List.of("import example.Forbidden;")
        );

        //then
        assertEquals(
                List.of(
                        firstFile + " contains forbidden import: import example.Forbidden;",
                        secondFile + " contains forbidden import: import example.Forbidden;"
                ),
                result
        );
    }

    @Test
    void should_continue_inspection_after_source_check_failure() throws IOException {
        //given
        Path invalidFile = writeSource("A.java", "invalid source");
        Path validFile = writeSource("B.java", "class B {}");

        JavaSourceImportCheck check = source -> {
            if (source.equals("invalid source")) {
                throw new IllegalArgumentException("invalid source");
            }

            return List.of("import example.Forbidden;");
        };

        //when
        List<String> result = inspector.inspectDirectory(tempDir, check);

        //then
        assertEquals(
                List.of(
                        invalidFile + " could not be inspected: invalid source",
                        validFile + " contains forbidden import: import example.Forbidden;"
                ),
                result
        );
    }

    @Test
    void should_return_empty_list_when_directory_has_no_java_sources() throws IOException {
        //given
        writeSource("README.txt", "not a Java source");

        //when
        List<String> result = inspector.inspectDirectory(
                tempDir,
                source -> fail("Check must not be called without Java sources")
        );

        //then
        assertEquals(List.of(), result);
    }

    @Test
    void should_return_empty_list_when_directory_is_empty() throws IOException {
        //when
        List<String> result = inspector.inspectDirectory(
                tempDir,
                source -> fail("check must not be called for an empty directory")
        );

        //then
        assertEquals(List.of(), result);
    }

    @Test
    void should_fail_when_source_directory_does_not_exist() {
        //given
        Path sourceDirectory = tempDir.resolve("missing");

        //when & then
        assertThrows(
                IOException.class,
                () -> inspector.inspectDirectory(
                        sourceDirectory,
                        source -> List.of()
                )
        );
    }

    @Test
    void should_reject_file_instead_of_source_directory() throws IOException {
        //given
        Path sourceFile = writeSource("Example.java", "class Example {}");

        //when & then
        assertThrows(
                NotDirectoryException.class,
                () -> inspector.inspectDirectory(
                        sourceFile,
                        source -> List.of()
                )
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void should_return_immutable_results(
            boolean inspectDirectory
    ) throws IOException {
        //given
        Path sourceFile = writeSource("Example.java", "class Example {}");

        JavaSourceImportCheck check = source -> List.of("import example.Forbidden;");

        List<String> result = inspectDirectory
                ? inspector.inspectDirectory(tempDir, check)
                : inspector.inspectFile(sourceFile, check);

        //when & then
        assertThrows(
                UnsupportedOperationException.class,
                () -> result.add("unexpected diagnostic")
        );
    }

    @Test
    void should_reject_null_source_directory() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> inspector.inspectDirectory(null, source -> List.of())
        );

        assertEquals("sourceDirectory", exception.getMessage());
    }

    @Test
    void should_reject_null_source_file() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> inspector.inspectFile(null, source -> List.of())
        );

        assertEquals("sourceFile", exception.getMessage());
    }

    @Test
    void should_reject_null_check_before_reading_source_file() {
        //given
        Path sourceFile = tempDir.resolve("Missing.java");

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> inspector.inspectFile(sourceFile, null)
        );

        assertEquals("check", exception.getMessage());
    }

    @Test
    void should_reject_null_check_for_empty_directory() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> inspector.inspectDirectory(tempDir, null)
        );

        assertEquals("check", exception.getMessage());
    }

    private static Stream<JavaSourceImportCheck> failedChecks() {
        return Stream.<JavaSourceImportCheck>of(
                source -> {
                    throw new IOException("inspection failed");
                },
                source -> {
                    throw new IllegalArgumentException("inspection failed");
                }
        );
    }

    private Path writeSource(
            String relativePath,
            String source
    ) throws IOException {
        Path sourceFile = tempDir.resolve(relativePath);

        Files.createDirectories(sourceFile.getParent());

        return Files.writeString(sourceFile, source);
    }
}
