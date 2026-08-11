package com.launcher.core.architecture.support.recording;

import com.launcher.core.event.Event;
import com.launcher.core.event.EventBus;

import java.util.ArrayList;
import java.util.List;

public final class RecordingEventBus extends EventBus {

    private final List<Object> events = new ArrayList<>();

    @Override
    public <T extends Event> void publish(T event) {
        events.add(event);
    }

    public <T> List<T> eventsOfType(Class<T> type) {
        return events.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .toList();
    }

    public <T> T firstEventOfType(Class<T> type) {
        return eventsOfType(type).getFirst();
    }
}
