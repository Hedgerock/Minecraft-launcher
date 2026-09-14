package com.launcher.app.runtime.detection;

import com.launcher.core.runtime.detection.JavaRuntimeVersionDetector;
import com.launcher.core.runtime.detection.model.JavaRuntimeVersionDetectionRequest;
import com.launcher.model.runtime.JavaExecutableReference;
import com.launcher.model.runtime.JavaRuntimeVersion;

import java.util.Objects;

public final class DefaultJavaRuntimeVersionDetector implements JavaRuntimeVersionDetector {
    private final JavaRuntimeVersionCommandRunner commandRunner;
    private final JavaRuntimeVersionOutputParser outputParser;

    public DefaultJavaRuntimeVersionDetector() {
        this(
                new ProcessJavaRuntimeVersionCommandRunner(),
                new JavaRuntimeVersionOutputParser()
        );
    }

    DefaultJavaRuntimeVersionDetector(
            JavaRuntimeVersionCommandRunner commandRunner,
            JavaRuntimeVersionOutputParser outputParser
    ) {
        this.commandRunner = Objects.requireNonNull(commandRunner, "commandRunner");
        this.outputParser = Objects.requireNonNull(outputParser, "outputParser");
    }

    @Override
    public JavaRuntimeVersion detect(JavaRuntimeVersionDetectionRequest request) {
        Objects.requireNonNull(request, "request");

        JavaExecutableReference javaExecutableReference =
                request.resolvedJavaExecutableReference();

        if (!javaExecutableReference.isExplicitPath()) {
            throw new JavaRuntimeVersionDetectionException(
                    "Java executable reference must be an explicit path"
            );
        }

        JavaRuntimeVersionCommandResult commandResult = commandRunner.run(javaExecutableReference);

        if (!commandResult.isSuccessful()) {
            JavaProcessDiagnostic diagnostic = commandResult.toDiagnostic();

            throw new JavaRuntimeVersionDetectionException(diagnostic.message());
        }

        if (commandResult.output().isBlank()) {
            JavaProcessDiagnostic diagnostic = JavaProcessDiagnostic.emptyOutput();

            throw new JavaRuntimeVersionDetectionException(diagnostic.message());
        }

        try {
            return outputParser.parse(commandResult.output());
        } catch (JavaRuntimeVersionParsingException e) {
            JavaProcessDiagnostic diagnostic = JavaProcessDiagnostic.outputParsingFailed();

            throw new JavaRuntimeVersionDetectionException(diagnostic.message());
        }
    }
}
