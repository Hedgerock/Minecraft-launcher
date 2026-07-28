package com.launcher.core.architecture.support;

import java.util.ArrayList;
import java.util.List;

public final class RecordingEvents {

    private final List<String> events = new ArrayList<>();

    public void record(String event) {
        events.add(event);
    }

    public List<String> events() {
        return List.copyOf(events);
    }
}
