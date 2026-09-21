package com.launcher.app.support;

import com.launcher.app.result.LauncherResultHandler;
import com.launcher.core.LaunchResult;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class RecordingLauncherResultHandler implements LauncherResultHandler {
    private final AtomicReference<LaunchResult> result = new AtomicReference<>();
    private final CountDownLatch handled = new CountDownLatch(1);

    public boolean awaitHandled(long timeout, TimeUnit unit) throws InterruptedException {
        return handled.await(timeout, unit);
    }

    @Override
    public void handle(LaunchResult result) {
        this.result.set(result);
        handled.countDown();
    }

    public LaunchResult getResult() {
        return result.get();
    }
}
