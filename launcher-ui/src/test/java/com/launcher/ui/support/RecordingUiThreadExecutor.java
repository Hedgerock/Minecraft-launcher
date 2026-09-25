package com.launcher.ui.support;

import com.launcher.ui.UiThreadExecutor;

public final class RecordingUiThreadExecutor implements UiThreadExecutor {
    private Runnable action;

    @Override
    public void execute(Runnable action) {
        this.action = action;
    }

    public Runnable getAction() {
        return action;
    }
}
