package com.launcher.core.architecture.support;

import com.launcher.core.launch.LaunchContext;
import com.launcher.core.result.Result;
import com.launcher.core.state.LauncherState;
import com.launcher.core.task.LauncherTask;

public final class RecordingLauncherTask implements LauncherTask {
    private final RecordingEvents events;
    private final String eventName;
    private final Result result;

    public RecordingLauncherTask(RecordingEvents events, String eventName, Result result) {
        this.events = events;
        this.eventName = eventName;
        this.result = result;
    }

    @Override
    public LauncherState state() {
        return null;
    }

    @Override
    public Result execute(LaunchContext launchContext) {
        events.record(eventName);

        return result;
    }
}
