package com.launcher.downloader.download;

import com.launcher.downloader.exception.DownloadException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

final class StreamResourceTransfer implements ResourceTransfer {

    private final DownloadSource source;

    StreamResourceTransfer(DownloadSource source) {
        this.source = Objects.requireNonNull(source, "source");
    }

    @Override
    public void transfer(
            String url,
            Path temporaryFile,
            Path targetPath,
            long expectedSize,
            ResourceDownloadLimits limits
    ) throws IOException {
        byte[] buffer = new byte[8192];
        long writtenBytes = 0;

        try (
                InputStream input = source.open(url);
                OutputStream output = Files.newOutputStream(
                        temporaryFile,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING
                )
        ) {
            while (true) {
                long remainingBytes = expectedSize - writtenBytes;
                int readLimit = remainingBytes >= buffer.length
                        ? buffer.length
                        : (int) remainingBytes + 1;

                int count = input.read(buffer, 0, readLimit);

                if (count == -1) {
                    break;
                }

                if (count > remainingBytes) {
                    throw DownloadException.sizeMismatch(url, targetPath);
                }

                output.write(buffer, 0, count);
                writtenBytes += count;
            }

            if (writtenBytes != expectedSize) {
                throw DownloadException.sizeMismatch(url, targetPath);
            }
        }
    }
}
