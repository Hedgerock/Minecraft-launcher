package com.launcher.ui;

@FunctionalInterface
public interface UiThreadExecutor {

    void execute(Runnable action);
}
