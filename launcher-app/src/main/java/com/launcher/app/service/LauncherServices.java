package com.launcher.app.service;

import com.launcher.core.download.DownloadService;
import com.launcher.core.manifest.ManifestService;
import com.launcher.core.storage.service.DirectoryService;
import com.launcher.core.verification.VerificationService;

public record LauncherServices(
        ManifestService manifestService,
        VerificationService verificationService,
        DirectoryService directoryService,
        DownloadService downloadService
) {
}
