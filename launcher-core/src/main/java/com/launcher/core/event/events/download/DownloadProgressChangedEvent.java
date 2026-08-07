package com.launcher.core.event.events.download;

import com.launcher.core.event.Event;

public record DownloadProgressChangedEvent(
        int downloadedFiles,
        int totalFiles,
        long downloadedBytes,
        long totalBytes
) implements Event {
}
