package com.launcher.app.support;

import com.launcher.app.presentation.completion.PresentationLaunchCompletion;
import com.launcher.app.presentation.completion.PresentationLaunchCompletionHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class RecordingPresentationLaunchCompletionHandler implements PresentationLaunchCompletionHandler {
    private final AtomicReference<PresentationLaunchCompletion> result = new AtomicReference<>();
    private final CountDownLatch handled = new CountDownLatch(1);

    public boolean awaitHandled(long timeout, TimeUnit unit) throws InterruptedException {
        return handled.await(timeout, unit);
    }

    @Override
    public void handle(PresentationLaunchCompletion completion) {
        this.result.set(completion);
        handled.countDown();
    }

    public PresentationLaunchCompletion getResult() {
        return result.get();
    }
}
