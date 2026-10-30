package com.launcher.testing.architecture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaSourceImportParserTest {

    private final JavaSourceImportParser parser = new JavaSourceImportParser();

    @ParameterizedTest
    @CsvSource({
            "import java.util.List;, java.util.List, false",
            "import static java.util.Objects.requireNonNull;, java.util.Objects.requireNonNull, true",
            "import java.util.*;, java.util.*, false",
            "import static java.util.Objects.*;, java.util.Objects.*, true",
            "import java.util.Map.Entry;, java.util.Map.Entry, false"
    })
    void should_parse_import_description(
            String declaration,
            String qualifiedName,
            boolean isStatic
    ) throws IOException {
        //given
        String source = sourceWithImports(declaration);

        //when
        List<JavaSourceImport> result = parser.parse(source);

        //then
        assertEquals(
                List.of(new JavaSourceImport(qualifiedName, isStatic)),
                result
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "import   static   java.util.Objects.requireNonNull;",
            "import\nstatic\njava.util.Objects.requireNonNull;",
            "import\r\nstatic\r\njava.util.Objects.requireNonNull;",
            "import /* comment */ static java.util.Objects.requireNonNull; // note"
    })
    void should_parse_import_independently_of_formatting(
            String declaration
    ) throws IOException {
        //given
        String source = sourceWithImports(declaration);

        //when
        List<JavaSourceImport> result = parser.parse(source);

        //then
        assertEquals(
                List.of(new JavaSourceImport("java.util.Objects.requireNonNull", true)),
                result
        );
    }

    @Test
    void should_preserve_import_order_and_duplicates() throws IOException {
        //given
        String source = sourceWithImports("""
                import example.First;
                import static example.Second.VALUE;
                import example.First;
                """);

        //when
        List<JavaSourceImport> result = parser.parse(source);

        //then
        assertEquals(
                List.of(
                        new JavaSourceImport("example.First", false),
                        new JavaSourceImport("example.Second.VALUE", true),
                        new JavaSourceImport("example.First", false)
                ),
                result
        );
    }

    @Test
    void should_return_imports_without_applying_module_policies() throws IOException {
        //given
        String source = sourceWithImports("""
                import com.launcher.app.presentation.DefaultPresentationLaunchBoundary;
                import com.launcher.core.LaunchResult;
                import com.launcher.model.manifest.Manifest;
                """);

        //when
        List<JavaSourceImport> result = parser.parse(source);

        //then
        assertEquals(
                List.of(
                        new JavaSourceImport(
                                "com.launcher.app.presentation.DefaultPresentationLaunchBoundary",
                                false
                        ),
                        new JavaSourceImport(
                                "com.launcher.core.LaunchResult",
                                false
                        ),
                        new JavaSourceImport(
                                "com.launcher.model.manifest.Manifest",
                                false
                        )
                ),
                result
        );
    }

    @Test
    void should_parse_without_resolving_project_types() throws IOException {
        //given
        String source = """
                import example.missing.NotOnClasspath;
                class Example {
                    UnavailableType value;
                }
                """;

        //when
        List<JavaSourceImport> result = parser.parse(source);

        //then
        assertEquals(
                List.of(new JavaSourceImport("example.missing.NotOnClasspath", false)),
                result
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "// import example.Hidden;\nclass Example {}",
            "/*\nimport example.Hidden;\n*/\nclass Example {}",
            "class Example { String text = \"import example.Hidden;\"; }"
    })
    void should_ignore_import_text_in_comments_and_literals(
            String source
    ) throws IOException {
        //when
        List<JavaSourceImport> result = parser.parse(source);

        //then
        assertEquals(List.of(), result);
    }

    @Test
    void should_ignore_import_text_inside_text_block() throws IOException {
        //given
        String delimiter = "\"\"\"";

        String source = "class Example {\n" +
                "    String text = " + delimiter + "\n" +
                "        import example.Hidden;\n" +
                "        " + delimiter + ";\n" +
                "}\n";

        //when
        List<JavaSourceImport> result = parser.parse(source);

        //then
        assertEquals(List.of(), result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " \t\n",
            "// no imports\nclass Example {}"
    })
    void should_return_empty_list_when_source_has_no_imports(
            String source
    ) throws IOException {
        //when
        List<JavaSourceImport> result = parser.parse(source);

        //then
        assertEquals(List.of(), result);
    }

    @Test
    void should_return_immutable_import_list() throws IOException {
        //given
        String source = sourceWithImports("import java.util.List;");
        List<JavaSourceImport> result = parser.parse(source);

        //when & then
        assertThrows(
                UnsupportedOperationException.class,
                () -> result.add(new JavaSourceImport("java.util.Map", false))
        );
    }

    @Test
    void should_reject_invalid_source_even_when_imports_can_be_read() {
        //given
        String source = """
                import java.util.List;
                class Example {
                """;

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(source)
        );
    }

    @Test
    void should_reject_null_source() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> parser.parse(null)
        );
    }

    private String sourceWithImports(String declarations) {
        return declarations + "\nclass Example {}\n";
    }
}
