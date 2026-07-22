package com.launcher.core.service;

import com.launcher.core.manifest.ManifestService;
import com.launcher.storage.service.DirectoryService;

public record LauncherServices(
        ManifestService manifestService,
        DirectoryService directoryService
) {
}
