package com.launcher.ui.result;

@FunctionalInterface
interface UiThreadExecutor {

    void execute(Runnable action);
}
