package com.launcher.downloader.download;

import com.launcher.downloader.exception.DownloadException;
import com.launcher.downloader.exception.DownloadExceptionReason;
import com.launcher.storage.resolver.LocalWriteTargetResolver;
import com.launcher.testing.http.LocalHttpServer;
import com.launcher.testing.http.PausedHttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(30)
class HttpResourceTransferTest {

    private static final ResourceDownloadLimits DEFAULT_LIMITS = new ResourceDownloadLimits(
            Duration.ofSeconds(10),
            64 * 1024
    );

    private static final Duration PAUSED_RESPONSE_TIMEOUT = Duration.ofSeconds(30);

    private static final ResourceDownloadLimits DEADLINE_LIMITS = new ResourceDownloadLimits(
            Duration.ofSeconds(5),
            64 * 1024
    );

    @TempDir
    Path tempDir;

    private LocalHttpServer httpServer;
    private HttpClient httpClient;
    private HttpResourceTransfer httpResourceTransfer;

    private Path temporaryFile;
    private Path targetPath;

    @BeforeEach
    void setUp() throws IOException {
        temporaryFile = Files.createTempFile(
                tempDir,
                "resource-",
                ".download"
        );

        targetPath = tempDir.resolve("resource.jar");

        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();

        httpResourceTransfer = new HttpResourceTransfer(httpClient);

        httpServer = LocalHttpServer.bind();
        httpServer.start();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        try {
            if (httpServer != null) {
                httpServer.close();
            }
        } finally {
            if (httpClient != null) {
                httpClient.shutdownNow();

                assertTrue(
                        httpClient.awaitTermination(Duration.ofSeconds(5)),
                        "HTTP client did not terminate"
                );
            }
        }
    }

    @Test
    void should_write_only_final_response_when_technical_download_is_redirected() throws IOException {
        //given
        String path = "/redirected-resource";
        String destination = "/final-resource";
        byte[] content = {1, 2, 3};

        Path trustedRoot = tempDir.resolve("game");
        Path target = trustedRoot.resolve("libraries/library.jar");

        httpServer.createContext(path, exchange -> {
            try (exchange) {
                byte[] redirectBody = {9, 9, 9, 9};
                exchange.getResponseHeaders().set("Location", destination);
                exchange.sendResponseHeaders(302, redirectBody.length);
                exchange.getResponseBody().write(redirectBody);
            }
        });

        registerResponse(destination, content);
        FileDownloader downloader = technicalDownloader(DEFAULT_LIMITS);

        //when
        downloader.download(url(path), trustedRoot, target, content.length);

        //then
        assertArrayEquals(content, Files.readAllBytes(target));
        assertOnlyTargetRemains(target);
    }

    @Test
    void should_timeout_when_response_headers_are_not_sent() throws Exception {
        //given
        String path = "/pending-headers";
        CountDownLatch requestReceived = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        httpServer.createContext(path, exchange -> {
           try (exchange) {
               requestReceived.countDown();
               awaitResponseRelease(release);
           }
        });

        try {
            //when
            IOException failure = assertThrows(
                    IOException.class,
                    () -> transfer(path, 1, DEADLINE_LIMITS)
            );

            //then
            assertTrue(
                    requestReceived.await(5, TimeUnit.SECONDS),
                    "Server did not receive request"
            );

            assertInstanceOf(TimeoutException.class, failure.getCause());

            Files.delete(temporaryFile);
            assertFalse(Files.exists(temporaryFile));
        } finally {
            release.countDown();
        }
    }

    @Test
    void should_preserve_existing_target_when_technical_download_times_out() throws Exception {
        //given
        String path = "/late-resource";
        byte[] original = {9, 8};
        Path trustedRoot = tempDir.resolve("game");
        Path target = trustedRoot.resolve("libraries/library.jar");

        Files.createDirectories(target.getParent());
        Files.write(target, original);

        PausedHttpResponse response = httpServer.registerPausedBody(
                path, 200, new byte[]{1}, new byte[]{2}, PAUSED_RESPONSE_TIMEOUT
        );

        FileDownloader downloader = technicalDownloader(DEADLINE_LIMITS);

        try {
            //when
            DownloadException failure = assertThrows(
                    DownloadException.class,
                    () -> downloader.download(url(path), trustedRoot, target, 2)
            );

            //then
            assertTrue(
                    response.awaitStarted(Duration.ofSeconds(5)),
                    "Server did not send the first body chunk"
            );

            assertEquals(DownloadExceptionReason.DOWNLOAD_FAILED, failure.getReason());

            IOException transferFailure = assertInstanceOf(IOException.class, failure.getCause());
            assertInstanceOf(TimeoutException.class, transferFailure.getCause());

            assertExchangeTerminatedWhileResponsePaused(response);
            assertArrayEquals(original, Files.readAllBytes(target));
            assertOnlyTargetRemains(target);

            response.release();
            assertTrue(
                    response.awaitFinished(Duration.ofSeconds(5)),
                    "Server did not finish the response"
            );

            assertArrayEquals(original, Files.readAllBytes(target));
            assertOnlyTargetRemains(target);
        } finally {
            response.release();
        }
    }

    @Test
    void should_reject_redirect_response() {
        //given
        String path = "/redirect";
        AtomicInteger redirectRequests = new AtomicInteger();

        httpServer.createContext(path, exchange -> {
           try (exchange) {
               exchange.getResponseHeaders()
                       .set("Location", "/redirect-target");

               exchange.sendResponseHeaders(302, -1);
           }
        });

        httpServer.createContext("/redirect-target", exchange -> {
           redirectRequests.incrementAndGet();

           try (exchange) {
               exchange.sendResponseHeaders(200, 1);
               exchange.getResponseBody().write(new byte[]{1});
           }
        });

        //when
        IOException failure = assertThrows(
                IOException.class,
                () -> transfer(path, 1, DEFAULT_LIMITS)
        );

        //then
        assertEquals(
                "Unexpected resource HTTP status: 302",
                failure.getMessage()
        );

        assertEquals(0, redirectRequests.get());
    }

    @Test
    void should_not_restart_deadline_after_technical_redirect() {
        //given
        String path = "/slow-redirect";
        String destination = "/slow-final";
        CountDownLatch finalRequestReceived = new CountDownLatch(1);

        httpServer.createContext(path, exchange -> {
            try (exchange) {
                delayTestResponse();
                exchange.getResponseHeaders().set("Location", destination);
                exchange.sendResponseHeaders(302, -1);
            }
        });

        httpServer.createContext(destination, exchange -> {
            try (exchange) {
                finalRequestReceived.countDown();
                delayTestResponse();
                exchange.sendResponseHeaders(200, 1);
                exchange.getResponseBody().write(1);
            }
        });

        //when
        IOException failure = assertThrows(
                IOException.class,
                () -> httpResourceTransfer.transferTechnical(
                        url(path), temporaryFile, targetPath, 1, DEADLINE_LIMITS
                )
        );

        //then
        assertEquals(0L, finalRequestReceived.getCount(), "Redirect was not followed");
        assertInstanceOf(TimeoutException.class, failure.getCause());
    }

    @Test
    void should_reject_chunked_body_before_writing_excess_bytes() throws IOException {
        //given
        String path = "/excess";
        registerChunkedResponse(path, new byte[]{1, 2, 3});

        //when
        DownloadException failure = assertThrows(
                DownloadException.class,
                () -> transfer(path, 2, DEFAULT_LIMITS)
        );

        //then
        assertEquals(
                DownloadExceptionReason.SIZE_MISMATCH,
                failure.getReason()
        );

        assertTrue(
                Files.size(temporaryFile) <= 2,
                "Temporary file must not exceed the expected size"
        );
    }

    @Test
    void should_restore_interrupt_flag_when_transfer_is_interrupted() throws Exception {
        //given
        String path = "/interrupted";

        PausedHttpResponse response = httpServer.registerPausedBody(
                path, 200, new byte[]{1}, new byte[]{2}, PAUSED_RESPONSE_TIMEOUT
        );

        ResourceDownloadLimits limits = new ResourceDownloadLimits(
                Duration.ofSeconds(30),
                64 * 1024
        );

        FileDownloader downloader = technicalDownloader(limits);

        byte[] original = {9, 8};
        Path trustedRoot = tempDir.resolve("game");
        Path target = trustedRoot.resolve("libraries/library.jar");

        Files.createDirectories(target.getParent());
        Files.write(target, original);

        FutureTask<InterruptedCall> call = new FutureTask<>(() -> {
            DownloadException failure = assertThrows(
                    DownloadException.class,
                    () -> downloader.download(url(path), trustedRoot, target, 2)
            );

            return new InterruptedCall(
                    failure,
                    Thread.currentThread().isInterrupted()
            );
        });

        //when
        Thread worker = Thread.ofPlatform()
                .daemon()
                .start(call);

        try {
            assertTrue(
                    response.awaitStarted(Duration.ofSeconds(5)),
                    "Server did not send the first body chunk"
            );

            worker.interrupt();

            InterruptedCall result = call.get(
                    5,
                    TimeUnit.SECONDS
            );

            //then
            assertExchangeTerminatedWhileResponsePaused(response);

            assertTrue(result.interrupted(), "Download must restore the interrupt flag");
            IOException transferFailure = assertInstanceOf(IOException.class, result.failure().getCause());
            assertInstanceOf(InterruptedException.class, transferFailure.getCause());
            assertArrayEquals(original, Files.readAllBytes(target));
            assertOnlyTargetRemains(target);
        } finally {
            response.release();
            worker.interrupt();
            worker.join(5_000);
        }

        assertFalse(worker.isAlive(), "Transfer worker did not stop");
    }

    @Test
    void should_reject_expected_size_above_limit_before_opening_transfer() throws IOException {
        //given
        String path = "/limited";
        AtomicInteger requests = new AtomicInteger();
        byte[] originalContent = {7, 8};

        Files.write(temporaryFile, originalContent);

        httpServer.createContext(path, exchange -> {
           requests.incrementAndGet();

           try (exchange) {
               exchange.sendResponseHeaders(200, 3);
               exchange.getResponseBody().write(new byte[]{1, 2, 3});
           }
        });

        ResourceDownloadLimits limits = new ResourceDownloadLimits(
                Duration.ofSeconds(5),
                2
        );

        //when
        IllegalArgumentException failure = assertThrows(
                IllegalArgumentException.class,
                () -> transfer(path, 3, limits)
        );

        //then
        assertEquals(
                "expectedSize exceeds maxResourceBytes",
                failure.getMessage()
        );

        assertEquals(0, requests.get());

        assertArrayEquals(
                originalContent,
                Files.readAllBytes(temporaryFile)
        );
    }

    @Test
    void should_timeout_and_cancel_exchange_when_body_stops_arriving() throws Exception {
        //given
        String path = "/pending-body";

        PausedHttpResponse response = httpServer.registerPausedBody(
                path, 200, new byte[]{1}, new byte[]{2}, PAUSED_RESPONSE_TIMEOUT
        );

        try {
            //when
            IOException failure = assertThrows(
                    IOException.class,
                    () -> transfer(path, 2, DEADLINE_LIMITS)
            );

            //then
            assertTrue(
                    response.awaitStarted(Duration.ofSeconds(5)),
                    "Server did not send the first body chunk"
            );

            assertInstanceOf(
                    TimeoutException.class,
                    failure.getCause()
            );

            assertExchangeTerminatedWhileResponsePaused(response);

            Files.delete(temporaryFile);
            assertFalse(Files.exists(temporaryFile));
        } finally {
            response.release();
        }
    }

    @Test
    void should_reject_http_status_without_waiting_for_body() throws IOException {
        //given
        String path = "/missing";

        PausedHttpResponse response = httpServer.registerPausedBody(
                path, 404, new byte[0], PAUSED_RESPONSE_TIMEOUT
        );

        try {
            //when
            IOException failure = assertThrows(
                    IOException.class,
                    () -> transfer(path, 1, DEFAULT_LIMITS)
            );

            //then
            assertEquals(
                    "Unexpected resource HTTP status: 404",
                    failure.getMessage()
            );

            assertEquals(0L, Files.size(temporaryFile));
        } finally {
            response.release();
        }
    }

    @Test
    void should_reject_chunked_body_when_it_ends_before_expected_size() throws IOException {
        //given
        String path = "/short";
        byte[] content = {1, 2, 3};

        registerChunkedResponse(path, content);

        //when
        DownloadException failure = assertThrows(
                DownloadException.class,
                () -> transfer(path, 4, DEFAULT_LIMITS)
        );

        //then
        assertEquals(
                DownloadExceptionReason.SIZE_MISMATCH,
                failure.getReason()
        );

        assertEquals(url(path), failure.getUrl());
        assertEquals(
                targetPath,
                failure.getTargetPath().orElseThrow()
        );
        assertArrayEquals(
                content,
                Files.readAllBytes(temporaryFile)
        );
    }

    @Test
    void should_accept_empty_body_when_expected_size_is_zero() throws IOException {
        //given
        String path = "/empty";
        registerResponse(path, new byte[0]);

        //when
        transfer(path, 0, DEFAULT_LIMITS);

        //then
        assertTrue(Files.isRegularFile(temporaryFile));
        assertEquals(0L, Files.size(temporaryFile));
    }

    @Test
    void should_write_complete_binary_body_to_temporary_file() throws IOException {
        //given
        String path = "/resource";
        byte[] content = new byte[24 * 1024 + 3];

        for (int index = 0; index < content.length; index++) {
            content[index] = (byte) index;
        }

        registerResponse(path, content);

        //when
        transfer(path, content.length, DEFAULT_LIMITS);

        //then
        assertArrayEquals(
                content,
                Files.readAllBytes(temporaryFile)
        );

        assertFalse(Files.exists(targetPath));
    }

    private void transfer(
            String path,
            long expectedSize,
            ResourceDownloadLimits limits
    ) throws IOException {
        httpResourceTransfer.transfer(
                url(path),
                temporaryFile,
                targetPath,
                expectedSize,
                limits
        );
    }

    private String url(String path) {
        return httpServer.uri(path).toString();
    }

    private void registerResponse(String path, byte[] content) {
        httpServer.createContext(path, exchange -> {
           try (exchange) {
               if (content.length == 0) {
                   exchange.sendResponseHeaders(200, -1);
               } else {
                   exchange.sendResponseHeaders(200, content.length);
                   exchange.getResponseBody().write(content);
               }
           }
        });
    }

    private void registerChunkedResponse(
            String path,
            byte[] content
    ) {
        httpServer.createContext(path, exchange -> {
           try (exchange) {
               exchange.sendResponseHeaders(200, 0);
               exchange.getResponseBody().write(content);
           }
        });
    }

    private void assertExchangeTerminatedWhileResponsePaused(
            PausedHttpResponse response
    ) throws InterruptedException {
        httpClient.shutdown();

        assertTrue(
                httpClient.awaitTermination(Duration.ofSeconds(5)),
                "HTTP exchange must terminate while the response is paused"
        );
        assertFalse(
                response.awaitFinished(Duration.ZERO),
                "Server response must still be paused"
        );
    }

    private static void awaitResponseRelease(
            CountDownLatch release
    ) throws IOException {
        try {
            if (!release.await(30, TimeUnit.SECONDS)) {
                throw new IOException(
                        "Test did not release the response"
                );
            }
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();

            throw new IOException(
                    "Test response handler interrupted",
                    failure
            );
        }
    }

    private static void assertOnlyTargetRemains(Path target) throws IOException {
        try (var files = Files.list(target.getParent())) {
            assertEquals(List.of(target), files.toList());
        }
    }

    private static void delayTestResponse() throws IOException {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            throw new IOException("Test response handler interrupted", failure);
        }
    }

    private FileDownloader technicalDownloader(ResourceDownloadLimits limits) {
        return new DefaultFileDownloader(
                new TechnicalResourceTransfer(httpResourceTransfer),
                new LocalWriteTargetResolver(),
                limits
        );
    }

    private record InterruptedCall(
            DownloadException failure,
            boolean interrupted
    ) {}
}
