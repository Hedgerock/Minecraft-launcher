package com.launcher.downloader.download;

import java.io.IOException;
import java.nio.file.Path;

@FunctionalInterface
interface ResourceTransfer {

    void transfer(
            String url,
            Path temporaryFile,
            Path targetPath,
            long expectedSize,
            ResourceDownloadLimits limits
    ) throws IOException;
}
