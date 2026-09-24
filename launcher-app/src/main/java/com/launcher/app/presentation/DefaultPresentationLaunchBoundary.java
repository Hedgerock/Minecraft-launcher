package com.launcher.app.presentation;

import com.launcher.app.presentation.completion.PresentationLaunchCompletion;
import com.launcher.app.presentation.completion.PresentationLaunchCompletionHandler;
import com.launcher.app.presentation.report.PresentationLaunchDiagnosticReporter;
import com.launcher.app.presentation.report.PresentationLaunchDiagnosticSource;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DefaultPresentationLaunchBoundary implements PresentationLaunchBoundary {
    private final LauncherLifecycleRunner launcherLifecycleRunner;
    private final PresentationLaunchCompletionHandler presentationLaunchCompletionHandler;
    private final AtomicBoolean launchRunning = new AtomicBoolean();
    private final ExecutorService executorService;
    private final PresentationLaunchDiagnosticReporter presentationLaunchDiagnosticReporter;

    public DefaultPresentationLaunchBoundary(
            LauncherLifecycleRunner launcherLifecycleRunner,
            PresentationLaunchCompletionHandler presentationLaunchCompletionHandler,
            PresentationLaunchDiagnosticReporter presentationLaunchDiagnosticReporter
    ) {
        this(
                launcherLifecycleRunner,
                presentationLaunchCompletionHandler,
                presentationLaunchDiagnosticReporter,
                Executors.newSingleThreadExecutor()
        );
    }

    DefaultPresentationLaunchBoundary(
            LauncherLifecycleRunner launcherLifecycleRunner,
            PresentationLaunchCompletionHandler presentationLaunchCompletionHandler,
            PresentationLaunchDiagnosticReporter presentationLaunchDiagnosticReporter,
            ExecutorService executorService
    ) {
        this.launcherLifecycleRunner = Objects.requireNonNull(
                launcherLifecycleRunner, "launcherLifecycleRunner"
        );
        this.presentationLaunchCompletionHandler = Objects.requireNonNull(
                presentationLaunchCompletionHandler, "presentationLaunchCompletionHandler"
        );
        this.presentationLaunchDiagnosticReporter = Objects.requireNonNull(
                presentationLaunchDiagnosticReporter, "presentationLaunchDiagnosticReporter"
        );
        this.executorService = Objects.requireNonNull(
                executorService, "executorService"
        );
    }

    @Override
    public LaunchRequestResult requestLaunch() {
        boolean launchAcquired = launchRunning.compareAndSet(false, true);

        if (!launchAcquired) {
            return LaunchRequestResult.REJECTED_ALREADY_RUNNING;
        }

        try {
            executorService.execute(() -> {
                try {
                    PresentationLaunchCompletion completion;

                    try {
                        completion = PresentationLaunchCompletion.fromLaunchResult(launcherLifecycleRunner.launch());
                    } catch (RuntimeException e) {
                        completion = PresentationLaunchCompletion.executionFailure();

                        reportSafely(
                                PresentationLaunchDiagnosticSource.LAUNCH_EXECUTION,
                                e
                        );
                    }

                    try {
                        presentationLaunchCompletionHandler.handle(completion);
                    } catch (RuntimeException e) {
                        reportSafely(
                                PresentationLaunchDiagnosticSource.COMPLETION_HANDLER,
                                e
                        );
                    }
                } finally {
                    launchRunning.set(false);
                }
            });

            return LaunchRequestResult.ACCEPTED;
        } catch (RejectedExecutionException e) {
            launchRunning.set(false);
            throw e;
        }
    }

    @Override
    public void close() {
        executorService.shutdown();
    }

    private void reportSafely(
            PresentationLaunchDiagnosticSource source,
            Throwable cause
    ) {
        try {
            presentationLaunchDiagnosticReporter.report(
                    source,
                    cause
            );
        } catch (RuntimeException ignored) {
            // Diagnostic reporting must not affect presentation launch flow
        }
    }
}
