package com.launcher.ui.architecture.boundary;

import com.launcher.testing.architecture.JavaSourceImport;
import com.launcher.testing.architecture.JavaSourceImportParser;

import java.io.IOException;
import java.util.List;
import java.util.Set;

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

    private final JavaSourceImportParser parser = new JavaSourceImportParser();

    List<String> findForbiddenImports(String source) throws IOException {
        return parser.parse(source).stream()
                .filter(javaImport -> isForbiddenImport(
                        javaImport.qualifiedName(),
                        javaImport.declaration()
                ))
                .map(JavaSourceImport::declaration)
                .toList();
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
}
