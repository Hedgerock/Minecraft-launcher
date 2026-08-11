package com.launcher.core.architecture.support.recording;

import com.launcher.core.event.EventBus;
import com.launcher.core.state.LauncherState;
import com.launcher.core.state.LauncherStateMachine;

import java.util.ArrayList;
import java.util.List;

public final class RecordingLauncherStateMachine extends LauncherStateMachine {

    private final List<LauncherState> transitions = new ArrayList<>();

    public RecordingLauncherStateMachine(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    public void transition(LauncherState nextState) {
        transitions.add(nextState);
        super.transition(nextState);
    }

    public List<LauncherState> getTransitions() {
        return transitions;
    }
}
