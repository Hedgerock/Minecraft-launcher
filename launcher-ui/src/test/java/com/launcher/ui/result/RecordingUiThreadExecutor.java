package com.launcher.ui.result;

final class RecordingUiThreadExecutor implements UiThreadExecutor {
    private Runnable action;

    @Override
    public void execute(Runnable action) {
        this.action = action;
    }

    Runnable getAction() {
        return action;
    }
}
