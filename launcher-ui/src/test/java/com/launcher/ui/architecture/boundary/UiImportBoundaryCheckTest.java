package com.launcher.ui.architecture.boundary;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UiImportBoundaryCheckTest {

    private final UiImportBoundaryCheck checker = new UiImportBoundaryCheck();

    @Test
    void should_reject_syntactically_invalid_source() {
        //given
        String source = "class Example {";

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
    void should_report_forbidden_imports_in_source_order() throws IOException {
        //given
        String source = sourceWithImports("""
                import com.launcher.app.presentation.DefaultPresentationLaunchBoundary;
                import com.launcher.core.LaunchResult;
                import static com.launcher.core.LaunchResult.success;
                """);

        //when
        List<String> result = checker.findForbiddenImports(source);

        //then
        assertEquals(
                List.of(
                        "import com.launcher.app.presentation.DefaultPresentationLaunchBoundary;",
                        "import static com.launcher.core.LaunchResult.success;"
                ),
                result
        );
    }

    @Test
    void should_ignore_import_text_inside_text_block() throws IOException {
        //given
        String delimiter = "\"\"\"";

        String source = "class Example {\n" +
                "    String text = " + delimiter + "\n" +
                "        import com.launcher.app.internal.Adapter;\n" +
                "        " + delimiter + ";\n" +
                "}\n";

        //when
        List<String> result = checker.findForbiddenImports(source);

        //then
        assertEquals(List.of(), result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "// import com.launcher.app.internal.Adapter;\nclass Example {}",
            "/*\nimport com.launcher.app.internal.Adapter;\n*/\nclass Example {}",
            "class Example { String text = \"import com.launcher.core.internal.Adapter;\"; }"
    })
    void should_ignore_import_text_in_comments_and_literals(
            String source
    ) throws IOException {
        //when
        List<String> result = checker.findForbiddenImports(source);

        //then
        assertEquals(List.of(), result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "import java.util.List;",
            "import javafx.scene.control.Button;",
            "import static java.util.Objects.requireNonNull;",
            "import com.launcher.application.Adapter;",
            "import com.launcher.coreutils.Helper;"
    })
    void should_ignore_imports_outside_controlled_packages(
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
            "import   com.launcher.app.presentation.DefaultPresentationLaunchBoundary;",
            "import\ncom.launcher.app.presentation.DefaultPresentationLaunchBoundary;",
            "import\r\ncom.launcher.app.presentation.DefaultPresentationLaunchBoundary;",
            "import com.launcher.app.presentation.DefaultPresentationLaunchBoundary; // internal adapter"
    })
    void should_reject_internal_imports_independently_of_formatting(
            String declaration
    ) throws IOException {
        //given
        String source = sourceWithImports(declaration);

        //when
        List<String> result = checker.findForbiddenImports(source);

        //then
        assertEquals(
                List.of(
                        "import com.launcher.app.presentation.DefaultPresentationLaunchBoundary;"
                ),
                result
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "import   com.launcher.core.LaunchResult;",
            "import\ncom.launcher.app.presentation.PresentationLaunchBoundary;",
            "import com.launcher.core.LaunchFailure; // public lifecycle model",
            "import /* comment */ com.launcher.core.operation.type.OperationType;"
    })
    void should_allow_approved_imports_independently_of_formatting(
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
            "import static com.launcher.core.LaunchResult.success;",
            "import static com.launcher.core.operation.type.OperationType.LOAD_MANIFEST;",
            "import static com.launcher.app.bootstrap.Bootstrap.*;",
            "import static com.launcher.app.presentation.phase.PresentationLaunchPhase.LOADING_MANIFEST;"
    })
    void should_not_allow_static_imports_of_approved_types(
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
            "import com.launcher.app.presentation.DefaultPresentationLaunchBoundary;",
            "import com.launcher.core.engine.LauncherEngine;",
            "import com.launcher.app.configuration.*;",
            "import com.launcher.core.*;",
            "import com.launcher.core.LaunchResult.Nested;",
            "import com.launcher.app.bootstrap.Bootstrap.Nested;"
    })
    void should_reject_imports_outside_explicit_allowlists(
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
            "import com.launcher.core.LaunchResult;",
            "import com.launcher.core.LaunchFailure;",
            "import com.launcher.core.operation.type.OperationType;",
            "import com.launcher.app.bootstrap.Bootstrap;",
            "import com.launcher.app.configuration.LauncherConfigurationResolver;",
            "import com.launcher.app.presentation.LaunchRequestResult;",
            "import com.launcher.app.presentation.PresentationLaunchBoundary;",
            "import com.launcher.app.presentation.completion.PresentationLaunchCompletion;",
            "import com.launcher.app.presentation.completion.PresentationLaunchCompletionHandler;",
            "import com.launcher.app.presentation.phase.PresentationLaunchPhase;",
            "import com.launcher.app.presentation.phase.PresentationLaunchPhaseHandler;",
            "import com.launcher.app.configuration.ManifestUriConfigurationException;",
            "import com.launcher.app.configuration.path.LauncherUserPathsResolutionException;",
            "import com.launcher.app.configuration.LauncherDirectoryConfigurationException;",
            "import com.launcher.app.configuration.LocalManifestUriConfigurationException;",
            "import com.launcher.app.configuration.ResolvedLauncherConfiguration;"
    })
    void should_allow_explicitly_approved_imports(String declaration) throws IOException {
        //given
        String source = sourceWithImports(declaration);

        //when
        List<String> result = checker.findForbiddenImports(source);

        //then
        assertEquals(List.of(), result);
    }

    private String sourceWithImports(String declarations) {
        return declarations + "\nclass Example {}\n";
    }
}
