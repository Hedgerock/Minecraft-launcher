package com.launcher.downloader.download;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;

final class TechnicalResourceTransfer implements ResourceTransfer {

    private final HttpResourceTransfer httpTransfer;
    private final ResourceTransfer streamTransfer = new StreamResourceTransfer(url ->
            URI.create(url).toURL().openStream());

    TechnicalResourceTransfer(HttpResourceTransfer httpTransfer) {
        this.httpTransfer = Objects.requireNonNull(httpTransfer, "httpTransfer");
    }

    @Override
    public void transfer(
            String url,
            Path temporaryFile,
            Path targetPath,
            long expectedSize,
            ResourceDownloadLimits limits
    ) throws IOException {
        URI uri = URI.create(url);
        String scheme = uri.getScheme();

        if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
            httpTransfer.transferTechnical(url, temporaryFile, targetPath, expectedSize, limits);
            return;
        }

        streamTransfer.transfer(url, temporaryFile, targetPath, expectedSize, limits);
    }
}
