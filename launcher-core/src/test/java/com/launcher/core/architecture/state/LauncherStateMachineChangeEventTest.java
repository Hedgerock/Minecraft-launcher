package com.launcher.core.architecture.state;

import com.launcher.core.architecture.support.recording.RecordingEventBus;
import com.launcher.core.event.events.StateChangedEvent;
import com.launcher.core.state.LauncherState;
import com.launcher.core.state.LauncherStateMachine;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LauncherStateMachineChangeEventTest {

    @Test
    void should_publish_state_changed_event_with_previous_and_current_state() {
        //given
        RecordingEventBus eventBus = new RecordingEventBus();
        LauncherStateMachine stateMachine = new LauncherStateMachine(eventBus);

        stateMachine.transition(LauncherState.LOADING_MANIFEST);

        //when
        stateMachine.transition(LauncherState.VERIFYING_FILES);

        //then
        StateChangedEvent expectedChangeEvent = new StateChangedEvent(
                LauncherState.LOADING_MANIFEST,
                LauncherState.VERIFYING_FILES
        );

        assertEquals(
                expectedChangeEvent,
                eventBus.eventsOfType(StateChangedEvent.class).getLast()
        );
    }

    @Test
    void should_publish_state_changed_event_for_initial_transition() {

        //given
        RecordingEventBus eventBus = new RecordingEventBus();
        LauncherStateMachine stateMachine = new LauncherStateMachine(eventBus);


        //when
        stateMachine.transition(LauncherState.LOADING_MANIFEST);

        //then
        assertEquals(
                List.of(
                        new StateChangedEvent(
                                LauncherState.IDLE,
                                LauncherState.LOADING_MANIFEST
                        )
                ),
                eventBus.eventsOfType(StateChangedEvent.class)
        );

    }

}
