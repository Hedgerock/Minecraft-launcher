package com.launcher.core.infrastructure;

import com.launcher.api.http.HttpClient;
import com.launcher.core.event.EventBus;
import com.launcher.storage.file.FileStorage;

public record LauncherInfrastructure(
        HttpClient httpClient,
        FileStorage fileStorage,
        EventBus eventBus
) {
}
