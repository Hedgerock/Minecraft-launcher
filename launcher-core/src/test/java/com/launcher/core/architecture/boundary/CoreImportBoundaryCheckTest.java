package com.launcher.core.architecture.boundary;

import com.launcher.testing.architecture.JavaSourceImport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CoreImportBoundaryCheckTest {

    private final CoreImportBoundaryCheck checker = new CoreImportBoundaryCheck();

    @Test
    void should_use_shared_model_through_test_dependency() {
        //given
        JavaSourceImport javaSourceImport =
                new JavaSourceImport("java.util.List", false);

        //when
        String declaration = javaSourceImport.declaration();

        //then
        assertEquals("import java.util.List;", declaration);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "// import com.launcher.app.Adapter;\nclass Example {}",
            "/* import com.launcher.app.Adapter; */ class Example {}",
            "class Example { String text = \"import com.launcher.app.Adapter;\"; }"
    })
    void should_ignore_import_text_in_comments_and_literals(
            String source
    ) throws IOException {
        //when
        List<String> result = checker.findForbiddenImports(source);

        //then
        assertEquals(List.of(), result);
    }

    @Test
    void should_reject_null_source() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> checker.findForbiddenImports(null)
        );

        assertEquals("source", exception.getMessage());
    }

    @Test
    void should_reject_syntactically_invalid_source() {
        //given
        String source = """
                import com.launcher.app.Adapter;
                class Example {
                """;

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> checker.findForbiddenImports(source)
        );
    }

    @Test
    void should_allow_empty_source() throws IOException {
        //given
        String source = "";

        //when
        List<String> result = checker.findForbiddenImports(source);

        //then
        assertEquals(List.of(), result);
    }

    @Test
    void should_report_all_forbidden_imports_in_source_order() throws IOException {
        //given
        String source = sourceWithImports("""
                import com.launcher.app.FirstAdapter;
                import java.util.List;
                import static com.launcher.natives.SecondAdapter.VALUE;
                import com.launcher.storage.file.LocalFileStorage;
                """);

        //when
        List<String> result = checker.findForbiddenImports(source);

        //then
        assertEquals(
                List.of(
                        "import com.launcher.app.FirstAdapter;",
                        "import static com.launcher.natives.SecondAdapter.VALUE;",
                        "import com.launcher.storage.file.LocalFileStorage;"
                ),
                result
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "import   static   com.launcher.app.Adapter.VALUE;",
            "import\nstatic\ncom.launcher.app.Adapter.VALUE;"
    })
    void should_recognize_imports_independently_of_formatting(
            String declaration
    ) throws IOException {
        //given
        String source = sourceWithImports(declaration);

        //when
        List<String> result = checker.findForbiddenImports(source);

        //then
        assertEquals(
                List.of("import static com.launcher.app.Adapter.VALUE;"),
                result
        );
    }

    @Test
    void should_ignore_import_text_inside_text_block() throws IOException {
        //given
        String delimiter = "\"\"\"";

        String source = "class Example {\n" +
                "    String text = " + delimiter + "\n" +
                "        import com.launcher.app.Adapter;\n" +
                "        " + delimiter + ";\n" +
                "}\n";

        //when
        List<String> result = checker.findForbiddenImports(source);

        //then
        assertEquals(List.of(), result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "import com.launcher.core.LaunchResult;",
            "import com.launcher.model.manifest.Manifest;",
            "import com.launcher.storage.file.FileMetadataReader;",
            "import com.launcher.storage.hash.HashService;",
            "import com.launcher.application.Adapter;",
            "import com.launcher.storage.file.LocalFileStorageFactory;",
            "import java.util.List;",
            "import static java.util.Objects.requireNonNull;"
    })
    void should_allow_imports_outside_forbidden_boundaries(
            String declaration
    ) throws IOException {
        //given
        String source = sourceWithImports(declaration);

        //when
        List<String> result = checker.findForbiddenImports(source);

        //then
        assertEquals(List.of(), result);
    }

    @ParameterizedTest
    @MethodSource("forbiddenPackageImports")
    void should_reject_imports_from_forbidden_packages(
            String declaration
    ) throws IOException {
        //given
        String source = sourceWithImports(declaration);

        //when
        List<String> result = checker.findForbiddenImports(source);

        //then
        assertEquals(List.of(declaration), result);
    }

    @ParameterizedTest
    @MethodSource("forbiddenStorageImports")
    void should_reject_concrete_storage_adapter_imports(
            String declaration
    ) throws IOException {
        //given
        String source = sourceWithImports(declaration);

        //when
        List<String> result = checker.findForbiddenImports(source);

        //then
        assertEquals(List.of(declaration), result);
    }

    private static Stream<String> forbiddenPackageImports() {
        return Stream.of(
                "com.launcher.app",
                "com.launcher.ui",
                "com.launcher.api",
                "com.launcher.downloader",
                "com.launcher.verification",
                "com.launcher.game",
                "com.launcher.natives"
        ).flatMap(packageName -> Stream.of(
                "import " + packageName + ".Adapter;",
                "import static " + packageName + ".Adapter.VALUE;",
                "import " + packageName + ".*;",
                "import static " + packageName + ".Adapter.*;"
        ));
    }

    private static Stream<String> forbiddenStorageImports() {
        return Stream.of(
                "com.launcher.storage.file.LocalFileStorage",
                "com.launcher.storage.file.LocalFileMetadataReader",
                "com.launcher.storage.hash.Sha256HashService",
                "com.launcher.storage.resolver.LocalWriteTargetResolver"
        ).flatMap(className -> Stream.of(
                "import " + className + ";",
                "import " + className + ".Nested;",
                "import static " + className + ".VALUE;",
                "import static " + className + ".*;"
        ));
    }

    private String sourceWithImports(String declarations) {
        return declarations + "\nclass Example {}\n";
    }
}
