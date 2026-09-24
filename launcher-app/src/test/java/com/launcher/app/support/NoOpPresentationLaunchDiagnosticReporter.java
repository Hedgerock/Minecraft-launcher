package com.launcher.app.support;

import com.launcher.app.presentation.report.PresentationLaunchDiagnosticReporter;
import com.launcher.app.presentation.report.PresentationLaunchDiagnosticSource;

import java.util.Objects;

public final class NoOpPresentationLaunchDiagnosticReporter implements PresentationLaunchDiagnosticReporter {

    @Override
    public void report(PresentationLaunchDiagnosticSource source, Throwable cause) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(cause, "cause");
    }
}
