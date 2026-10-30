package com.launcher.app.architecture.boundary;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppImportBoundaryCheckTest {

    private final AppImportBoundaryCheck checker = new AppImportBoundaryCheck();

    @ParameterizedTest
    @MethodSource("forbiddenImports")
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
    @ValueSource(strings = {
            "import com.launcher.app.bootstrap.Bootstrap;",
            "import com.launcher.core.LaunchResult;",
            "import com.launcher.model.manifest.Manifest;",
            "import com.launcher.storage.file.LocalFileStorage;",
            "import com.launcher.api.Adapter;",
            "import com.launcher.downloader.Adapter;",
            "import com.launcher.verification.Adapter;",
            "import com.launcher.game.Adapter;",
            "import com.launcher.natives.Adapter;",
            "import com.sun.source.tree.ImportTree;",
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
    @ValueSource(strings = {
            "import com.launcher.uikit.Adapter;",
            "import javafxextras.Adapter;",
            "import com.sun.javafxextras.Adapter;"
    })
    void should_allow_imports_from_similarly_named_packages(
            String declaration
    ) throws IOException {
        //given
        String source = sourceWithImports(declaration);

        //when
        List<String> result = checker.findForbiddenImports(source);

        //then
        assertEquals(List.of(), result);
    }

    @Test
    void should_recognize_formatted_forbidden_imports() throws IOException {
        //given
        String source = sourceWithImports(
                "import\r\n" +
                        "    com.launcher.ui.Adapter;\r\n" +
                        "import /* formatting */ static   javafx.application.Platform.runLater;\r\n"
        );

        //when
        List<String> result = checker.findForbiddenImports(source);

        //then
        assertEquals(
                List.of(
                        "import com.launcher.ui.Adapter;",
                        "import static javafx.application.Platform.runLater;"
                ),
                result
        );
    }

    @Test
    void should_report_all_forbidden_imports_in_source_order() throws IOException {
        //given
        String source = sourceWithImports("""
                import javafx.scene.control.Button;
                import java.util.List;
                import static com.launcher.ui.Adapter.VALUE;
                import com.sun.javafx.application.PlatformImpl;
                """);

        //when
        List<String> result = checker.findForbiddenImports(source);

        //then
        assertEquals(
                List.of(
                        "import javafx.scene.control.Button;",
                        "import static com.launcher.ui.Adapter.VALUE;",
                        "import com.sun.javafx.application.PlatformImpl;"
                ),
                result
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "// import com.launcher.ui.Adapter;\nclass Example {}",
            "/* import javafx.scene.control.Button; */ class Example {}",
            "class Example { String text = \"import com.sun.javafx.application.PlatformImpl;\"; }"
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
    void should_ignore_import_text_inside_text_block() throws IOException {
        //given
        String delimiter = "\"\"\"";

        String source = "class Example {\n" +
                "    String text = " + delimiter + "\n" +
                "        import javafx.scene.control.Button;\n" +
                "        " + delimiter + ";\n" +
                "}\n";

        //when
        List<String> result = checker.findForbiddenImports(source);

        //then
        assertEquals(List.of(), result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "class Example {}"
    })
    void should_allow_source_without_imports(String source) throws IOException {
        //when
        List<String> result = checker.findForbiddenImports(source);

        //then
        assertEquals(List.of(), result);
    }

    @Test
    void should_reject_syntactically_invalid_source() {
        //given
        String source = """
                import javafx.scene.control.Button;
                class Example {
                """;

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> checker.findForbiddenImports(source)
        );
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

    private static Stream<String> forbiddenImports() {
        return Stream.of(
                "com.launcher.ui",
                "javafx",
                "com.sun.javafx"
        ).flatMap(packageName -> Stream.of(
           "import " + packageName + ".Adapter;",
           "import " + packageName + ".Adapter.Nested;",
           "import static " + packageName + ".Adapter.VALUE;",
           "import " + packageName + ".*;",
           "import static " + packageName + ".Adapter.*;"
        ));
    }

    private String sourceWithImports(String declarations) {
        return declarations + "\nclass Example {}\n";
    }
}
