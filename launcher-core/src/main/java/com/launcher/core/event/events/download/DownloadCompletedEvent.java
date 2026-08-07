package com.launcher.core.event.events.download;

import com.launcher.core.event.Event;

public record DownloadCompletedEvent(
        int totalFiles,
        long totalBytes
) implements Event {
}
