package com.launcher.app.architecture.boundary;

import com.launcher.testing.architecture.JavaSourceImport;
import com.launcher.testing.architecture.JavaSourceImportParser;

import java.io.IOException;
import java.util.List;

final class AppImportBoundaryCheck {

    private static final List<String> FORBIDDEN_IMPORT_PREFIXES = List.of(
            "com.launcher.ui.",
            "javafx.",
            "com.sun.javafx."
    );

    private final JavaSourceImportParser parser = new JavaSourceImportParser();

    List<String> findForbiddenImports(String source) throws IOException {
        return parser.parse(source).stream()
                .filter(javaSourceImport -> isForbiddenImport(javaSourceImport.qualifiedName()))
                .map(JavaSourceImport::declaration)
                .toList();
    }

    private boolean isForbiddenImport(String qualifiedName) {
        return FORBIDDEN_IMPORT_PREFIXES.stream()
                .anyMatch(qualifiedName::startsWith);
    }
}
