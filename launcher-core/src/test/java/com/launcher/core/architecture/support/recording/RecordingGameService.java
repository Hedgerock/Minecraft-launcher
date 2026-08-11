package com.launcher.core.architecture.support.recording;

import com.launcher.core.game.GameService;

public final class RecordingGameService implements GameService {
    private boolean isLaunchCalled = false;

    @Override
    public void launch() {
        isLaunchCalled = true;
    }

    public boolean isLaunchCalled() {
        return isLaunchCalled;
    }
}
