package com.launcher.app.support;

import com.launcher.app.presentation.report.PresentationLaunchDiagnosticReporter;
import com.launcher.app.presentation.report.PresentationLaunchDiagnosticSource;

public final class RecordingPresentationLaunchDiagnosticReporter implements PresentationLaunchDiagnosticReporter {
    private Throwable cause;
    private PresentationLaunchDiagnosticSource source;

    @Override
    public void report(PresentationLaunchDiagnosticSource source, Throwable cause) {
        this.cause = cause;
        this.source = source;
    }

    public Throwable getCause() {
        return cause;
    }

    public PresentationLaunchDiagnosticSource getSource() {
        return source;
    }
}
