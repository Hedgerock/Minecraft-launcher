package com.launcher.core.architecture.boundary;

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
import java.util.stream.Collectors;

final class CoreImportBoundaryCheck {

    private static final List<String> FORBIDDEN_PACKAGE_PREFIXES = List.of(
            "com.launcher.app.",
            "com.launcher.ui.",
            "com.launcher.api.",
            "com.launcher.downloader.",
            "com.launcher.verification.",
            "com.launcher.game.",
            "com.launcher.natives."
    );

    private static final List<String> FORBIDDEN_CLASSES = List.of(
            "com.launcher.storage.file.LocalFileStorage",
            "com.launcher.storage.file.LocalFileMetadataReader",
            "com.launcher.storage.hash.Sha256HashService",
            "com.launcher.storage.resolver.LocalWriteTargetResolver"
    );

    List<String> findForbiddenImports(String source) throws IOException {
        Objects.requireNonNull(source, "source");

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        if (compiler == null) {
            throw new IllegalStateException(
                    "Java compiler is not available. Run tests with a full JDK"
            );
        }

        DiagnosticCollector<JavaFileObject> diagnostics =
                new DiagnosticCollector<>();

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
                    .map(diagnostic ->
                            "line " + diagnostic.getLineNumber() + ": " +
                                    diagnostic.getMessage(Locale.ROOT))
                    .collect(Collectors.joining("; "));

            if (!parseErrors.isEmpty()) {
                throw new IllegalArgumentException(
                        "Cannot parse Java source: " + parseErrors
                );
            }

            List<String> violations = new ArrayList<>();

            for (CompilationUnitTree unit : units) {
                for (ImportTree declaration : unit.getImports()) {
                    String importName = declaration.getQualifiedIdentifier().toString();

                    if (isForbiddenImport(importName)) {
                        String prefix = declaration.isStatic()
                                ? "import static "
                                : "import ";

                        violations.add(prefix + importName + ";");
                    }
                }
            }

            return List.copyOf(violations);
        }
    }

    private boolean isForbiddenImport(String importName) {
        boolean forbiddenPackage = FORBIDDEN_PACKAGE_PREFIXES.stream()
                .anyMatch(importName::startsWith);

        boolean forbiddenClass = FORBIDDEN_CLASSES.stream()
                .anyMatch(className ->
                        importName.equals(className) ||
                                importName.startsWith(className + "."));

        return forbiddenPackage || forbiddenClass;
    }

    private static final class SourceFile extends SimpleJavaFileObject {
        private final String source;

        private SourceFile(String source) {
            super(
                    URI.create("string:///CoreBoundarySource.java"),
                    JavaFileObject.Kind.SOURCE
            );
            this.source = source;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return source;
        }
    }
}
