package com.launcher.core.event.events.download;

import com.launcher.core.event.Event;

public record DownloadStartedEvent(
        int totalFiles,
        long totalBytes
) implements Event {
}
