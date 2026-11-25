package com.launcher.downloader.download;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;

final class ManagedResourceTransfer implements ResourceTransfer {

    private final ResourceTransfer httpTransfer;

    ManagedResourceTransfer() {
        this(new HttpResourceTransfer()::transfer);
    }

    ManagedResourceTransfer(ResourceTransfer httpTransfer) {
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
        validateUri(url);
        httpTransfer.transfer(url, temporaryFile, targetPath, expectedSize, limits);
    }

    private static void validateUri(String url) throws IOException {
        if (url == null) {
            throw new IOException("Invalid managed resource URI");
        }

        URI uri;

        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException failure) {
            throw new IOException(
                    "Invalid managed resource URI",
                    failure
            );
        }

        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
            throw new IOException("Managed resource requires an HTTPS URI");
        }

        if (uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
            throw new IOException("Managed resource URI must not contain user info or a fragment");
        }
    }
}
