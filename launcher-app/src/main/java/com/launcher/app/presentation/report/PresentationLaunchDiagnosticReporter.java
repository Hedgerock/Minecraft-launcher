package com.launcher.app.presentation.report;

public interface PresentationLaunchDiagnosticReporter {

    void report(PresentationLaunchDiagnosticSource source, Throwable cause);
}
