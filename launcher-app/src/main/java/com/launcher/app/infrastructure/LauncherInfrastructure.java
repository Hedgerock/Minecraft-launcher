package com.launcher.app.infrastructure;

import com.launcher.api.http.LauncherHttpClient;
import com.launcher.core.event.EventBus;
import com.launcher.core.storage.file.FileStorage;

public record LauncherInfrastructure(
        LauncherHttpClient launcherHttpClient,
        FileStorage fileStorage,
        EventBus eventBus
) {
}
