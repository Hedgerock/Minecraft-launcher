package com.launcher.ui.architecture.boundary;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.util.JavacTask;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

final class UiImportBoundaryCheck {

    private static final String CORE_IMPORT_PREFIX = "com.launcher.core.";
    private static final String APPLICATION_IMPORT_PREFIX = "com.launcher.app.";

    private static final Set<String> ALLOWED_CORE_IMPORTS = Set.of(
            "import com.launcher.core.LaunchResult;",
            "import com.launcher.core.LaunchFailure;",
            "import com.launcher.core.operation.type.OperationType;"
    );

    private static final Set<String> ALLOWED_APPLICATION_IMPORTS = Set.of(
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
    );

    List<String> findForbiddenImports(String source) throws IOException {
        Objects.requireNonNull(source, "source");

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        if (compiler == null) {
            throw new IllegalStateException(
                    "Java compiler is not available. Run tests with a full JDK"
            );
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(
                diagnostics,
                Locale.ROOT,
                StandardCharsets.UTF_8
        )) {
            JavacTask task = (JavacTask) compiler.getTask(
                    new StringWriter(),
                    fileManager,
                    diagnostics,
                    List.of("-proc:none", "--release", "21"),
                    null,
                    List.of(new SourceFile(source))
            );

            List<CompilationUnitTree> units = new ArrayList<>();

            task.parse().forEach(units::add);

            String parseErrors = diagnostics.getDiagnostics().stream()
                    .filter(diagnostic -> diagnostic.getKind() == Diagnostic.Kind.ERROR)
                    .map(diagnostic -> "line " + diagnostic.getLineNumber() + ": " + diagnostic.getMessage(Locale.ROOT))
                    .collect(Collectors.joining("; "));

            if (!parseErrors.isEmpty()) {
                throw new IllegalArgumentException(
                  "Cannot parse source: " + parseErrors
                );
            }

            List<String> violations = new ArrayList<>();

            for (CompilationUnitTree unit : units) {
                for (ImportTree declaration : unit.getImports()) {
                    String importName = declaration.getQualifiedIdentifier().toString();

                    String prefix = declaration.isStatic() ? "import static " : "import ";

                    String normalizedDeclaration = prefix + importName + ";";

                    if (isForbiddenImport(importName, normalizedDeclaration)) {
                        violations.add(normalizedDeclaration);
                    }
                }
            }

            return List.copyOf(violations);
        }
    }

    private boolean isForbiddenImport(
            String importName,
            String declaration
    ) {
        if (importName.startsWith(CORE_IMPORT_PREFIX)) {
            return !ALLOWED_CORE_IMPORTS.contains(declaration);
        }

        if (importName.startsWith(APPLICATION_IMPORT_PREFIX)) {
            return !ALLOWED_APPLICATION_IMPORTS.contains(declaration);
        }

        return false;
    }

    private static final class SourceFile extends SimpleJavaFileObject {
        private final String source;

        private SourceFile(String source) {
            super(
                    URI.create("string:///UiBoundarySource.java"),
                    Kind.SOURCE
            );
            this.source = source;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return source;
        }
    }
}
