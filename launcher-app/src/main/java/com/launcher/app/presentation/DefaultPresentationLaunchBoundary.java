package com.launcher.app.presentation;

import com.launcher.app.result.LauncherResultHandler;
import com.launcher.core.LaunchResult;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DefaultPresentationLaunchBoundary implements PresentationLaunchBoundary {
    private final LauncherLifecycleRunner launcherLifecycleRunner;
    private final LauncherResultHandler launcherResultHandler;
    private final AtomicBoolean launchRunning = new AtomicBoolean();
    private final ExecutorService executorService;

    public DefaultPresentationLaunchBoundary(
            LauncherLifecycleRunner launcherLifecycleRunner,
            LauncherResultHandler launcherResultHandler
    ) {
        this(
                launcherLifecycleRunner,
                launcherResultHandler,
                Executors.newSingleThreadExecutor()
        );
    }

    DefaultPresentationLaunchBoundary(
            LauncherLifecycleRunner launcherLifecycleRunner,
            LauncherResultHandler launcherResultHandler,
            ExecutorService executorService
    ) {
        this.launcherLifecycleRunner = Objects.requireNonNull(
                launcherLifecycleRunner, "launcherLifecycleRunner"
        );
        this.launcherResultHandler = Objects.requireNonNull(
                launcherResultHandler, "launcherResultHandler"
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
                    LaunchResult result = launcherLifecycleRunner.launch();

                    launcherResultHandler.handle(result);
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
}
