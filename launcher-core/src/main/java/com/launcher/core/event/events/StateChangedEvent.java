package com.launcher.core.event.events;

import com.launcher.core.event.Event;
import com.launcher.core.state.LauncherState;

public record StateChangedEvent(
        LauncherState oldState,
        LauncherState newState
) implements Event {
}
