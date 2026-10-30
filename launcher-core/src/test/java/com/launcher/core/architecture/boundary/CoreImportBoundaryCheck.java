package com.launcher.core.architecture.boundary;

import com.launcher.testing.architecture.JavaSourceImport;
import com.launcher.testing.architecture.JavaSourceImportParser;

import java.io.IOException;
import java.util.List;

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

    private final JavaSourceImportParser parser = new JavaSourceImportParser();

    List<String> findForbiddenImports(String source) throws IOException {
        return parser.parse(source).stream()
                .filter(javaImport -> isForbiddenImport(javaImport.qualifiedName()))
                .map(JavaSourceImport::declaration)
                .toList();
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
}
