package com.launcher.app.support;

import com.launcher.app.presentation.report.PresentationLaunchDiagnosticReporter;
import com.launcher.app.presentation.report.PresentationLaunchDiagnosticSource;

public final class RecordingFailingPresentationLaunchDiagnosticReporter implements PresentationLaunchDiagnosticReporter {
    private PresentationLaunchDiagnosticSource source;
    private Throwable cause;

    @Override
    public void report(PresentationLaunchDiagnosticSource source, Throwable cause) {
        this.source = source;
        this.cause = cause;

        throw new RuntimeException("Diagnostic reporting failed");
    }

    public PresentationLaunchDiagnosticSource getSource() {
        return source;
    }

    public Throwable getCause() {
        return cause;
    }
}
