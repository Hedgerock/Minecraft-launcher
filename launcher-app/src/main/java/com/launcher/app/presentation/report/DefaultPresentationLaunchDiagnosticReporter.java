package com.launcher.app.presentation.report;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DefaultPresentationLaunchDiagnosticReporter implements PresentationLaunchDiagnosticReporter {
    private final Logger presentationLaunchDiagnosticLogger;

    public DefaultPresentationLaunchDiagnosticReporter() {
        this(Logger.getLogger(DefaultPresentationLaunchDiagnosticReporter.class.getName()));
    }

    DefaultPresentationLaunchDiagnosticReporter(Logger logger) {
        Objects.requireNonNull(logger, "logger");

        this.presentationLaunchDiagnosticLogger = logger;
    }

    private static final String LOG_MESSAGE_TEMPLATE =
            "Launch failure [%s]: %s";

    @Override
    public void report(
            PresentationLaunchDiagnosticSource source,
            Throwable cause
    ) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(cause, "cause");

        presentationLaunchDiagnosticLogger.log(
                Level.SEVERE,
                LOG_MESSAGE_TEMPLATE.formatted(
                        source,
                        cause.getClass().getName()
                )
        );
    }
}
