package com.launcher.downloader.download;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ManagedResourceTransferTest {

    private static final Path TEMPORARY_FILE = Path.of("library.jar.download");
    private static final Path TARGET_PATH = Path.of("libraries/library.jar");
    private static final ResourceDownloadLimits LIMITS = new ResourceDownloadLimits(
            Duration.ofSeconds(5),
            1024
    );

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            "https://example. org/library.jar",
            "https:/library.jar",
            "http://example.org/library.jar",
            "file:/library.jar",
            "libraries/library.jar",
            "https://user@example.org/library.jar",
            "https://example.org/library.jar#section"
    })
    void should_reject_invalid_managed_uri_before_transfer(String url) {
        //given
        ResourceTransfer resourceTransfer = new ManagedResourceTransfer(
                (source, temporaryFile, targetPath, expectedSize, limits) -> {
                    throw new AssertionError(
                            "Invalid managed URI reached the HTTP adapter"
                    );
                }
        );

        //when & then
        assertThrows(
                IOException.class,
                () -> resourceTransfer.transfer(
                        url,
                        TEMPORARY_FILE,
                        TARGET_PATH,
                        3,
                        LIMITS
                )
        );
    }

    @Test
    void should_forward_valid_managed_resource_and_limits() throws IOException {
        //given
        String url = "https://example.org/library.jar";
        List<TransferCall> calls = new ArrayList<>();

        ResourceTransfer resourceTransfer = new ManagedResourceTransfer(
                (source, temporaryFile, targetPath, expectedSize, limits) -> {
                    calls.add(new TransferCall(
                            source,
                            temporaryFile,
                            targetPath,
                            expectedSize,
                            limits
                    ));
                }
        );

        //when
        resourceTransfer.transfer(url, TEMPORARY_FILE, TARGET_PATH, 3, LIMITS);

        //then
        assertEquals(
                List.of(new TransferCall(url, TEMPORARY_FILE, TARGET_PATH, 3, LIMITS)),
                calls
        );
    }

    private record TransferCall(
            String url,
            Path temporaryFile,
            Path targetPath,
            long expectedSize,
            ResourceDownloadLimits limits
    ) {}
}
