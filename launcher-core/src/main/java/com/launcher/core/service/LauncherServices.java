package com.launcher.core.service;

import com.launcher.api.manifest.service.ManifestService;
import com.launcher.core.storage.service.DirectoryService;

public record LauncherServices(
        ManifestService manifestService,
        DirectoryService directoryService
) {
}
