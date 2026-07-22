package com.launcher.core.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {

    private final Map<Class<?>, List<EventListener<?>>> listeners = new ConcurrentHashMap<>();

    public <T extends Event> void subscribe (
            Class<T> type,
            EventListener<T> listener
    ) {
        listeners
                .computeIfAbsent(type, c -> new CopyOnWriteArrayList<>())
                .add(listener);
    }

    @SuppressWarnings("unchecked")
    public <T extends Event> void publish(T event) {
        List<EventListener<?>> registered =
                listeners.get(event.getClass());

        if (registered == null) {
            return;
        }

        for (EventListener<?> listener: registered) {
            ((EventListener<T>) listener).onEvent(event);
        }
    }
}
