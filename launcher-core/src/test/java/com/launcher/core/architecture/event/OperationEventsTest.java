package com.launcher.core.architecture.event;

import com.launcher.core.event.Event;
import com.launcher.core.event.events.OperationCompletedEvent;
import com.launcher.core.event.events.OperationFailedEvent;
import com.launcher.core.event.events.OperationStartedEvent;
import com.launcher.core.operation.type.OperationType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class OperationEventsTest {

    @Test
    void should_represent_operation_started_event() {
        //given when
        OperationStartedEvent event = new OperationStartedEvent(OperationType.LOAD_MANIFEST);

        //then

        assertInstanceOf(Event.class, event);
        assertEquals(OperationType.LOAD_MANIFEST, event.operationType());
    }

    @Test
    void should_represent_operation_completed_event() {
        //given when
        OperationCompletedEvent event = new OperationCompletedEvent(OperationType.LOAD_MANIFEST);

        //then

        assertInstanceOf(Event.class, event);
        assertEquals(OperationType.LOAD_MANIFEST, event.operationType());
    }

    @Test
    void should_represent_operation_failed_event() {
        //given when
        OperationFailedEvent event = new OperationFailedEvent(
                OperationType.LOAD_MANIFEST,
                "manifest load failed"
        );

        //then

        assertInstanceOf(Event.class, event);
        assertEquals(OperationType.LOAD_MANIFEST, event.operationType());
        assertEquals("manifest load failed", event.errorMessage());
    }
}
