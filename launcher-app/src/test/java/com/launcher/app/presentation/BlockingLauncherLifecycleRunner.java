package com.launcher.app.presentation;

import com.launcher.core.LaunchResult;
import com.launcher.core.state.LauncherState;

import java.util.concurrent.CountDownLatch;

final class BlockingLauncherLifecycleRunner implements LauncherLifecycleRunner {
    private final CountDownLatch started;
    private final CountDownLatch canComplete;

    BlockingLauncherLifecycleRunner(
            CountDownLatch started,
            CountDownLatch canComplete
    ) {
        this.started = started;
        this.canComplete = canComplete;
    }

    @Override
    public LaunchResult launch() {
        started.countDown();

        try {
            canComplete.await();

            return new LaunchResult(
                    true,
                    LauncherState.RUNNING
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
