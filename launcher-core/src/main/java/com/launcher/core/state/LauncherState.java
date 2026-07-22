package com.launcher.core.state;

public enum LauncherState {
    IDLE,
    CHECKING_UPDATES,
    LOADING_MANIFEST,
    VERIFYING_FILES,
    DOWNLOADING,
    PREPARING_GAME,
    LAUNCHING,
    RUNNING,
    FAILED
}
