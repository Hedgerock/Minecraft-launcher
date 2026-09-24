package com.launcher.app.assembly;

import com.launcher.core.LauncherEngine;
import com.launcher.core.event.EventListener;
import com.launcher.core.event.events.StateChangedEvent;

public interface ApplicationAssembly {
    LauncherEngine createEngine(EventListener<StateChangedEvent> stateListener);
    LauncherEngine createEngine();
}
