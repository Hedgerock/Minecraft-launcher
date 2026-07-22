package com.launcher.core.state;

import com.launcher.core.event.EventBus;
import com.launcher.core.event.events.StateChangedEvent;

public class LauncherStateMachine {

    private LauncherState currentState = LauncherState.IDLE;
    private final EventBus eventBus;

    public LauncherStateMachine(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public LauncherState getCurrentState() {
        return currentState;
    }

    public void transition(LauncherState nextState) {
        System.out.println(currentState + " -> " + nextState);

        LauncherState previousState = currentState;
        currentState = nextState;

        eventBus.publish(
                new StateChangedEvent(previousState, nextState)
        );
    }
}
