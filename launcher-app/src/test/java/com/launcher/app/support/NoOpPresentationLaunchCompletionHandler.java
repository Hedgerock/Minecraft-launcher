package com.launcher.app.support;

import com.launcher.app.presentation.completion.PresentationLaunchCompletion;
import com.launcher.app.presentation.completion.PresentationLaunchCompletionHandler;

import java.util.Objects;

public final class NoOpPresentationLaunchCompletionHandler implements PresentationLaunchCompletionHandler {

    @Override
    public void handle(PresentationLaunchCompletion completion) {
        Objects.requireNonNull(completion, "completion");
    }
}
