package com.launcher.preparer.source;

import com.launcher.testing.http.LocalHttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(30)
class DeadlineVanillaAssetResponseFetcherTest {

    private static final Duration HEADER_DEADLINE = Duration.ofSeconds(3);
    private static final Duration BODY_DEADLINE = Duration.ofSeconds(5);
    private static final Duration SERVER_PAUSE_LIMIT = Duration.ofSeconds(20);

    @Test
    void should_timeout_when_server_does_not_send_headers() throws Exception {
        //given
        String path = "/pending-headers";
        CountDownLatch requestReceived = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        try (
                HttpClient client = createClient();
                LocalHttpServer server = LocalHttpServer.bind()
        ) {
            server.createContext(path, exchange -> {
                try (exchange) {
                    requestReceived.countDown();
                    awaitRelease(release);
                }
            });
            server.start();

            DeadlineVanillaAssetResponseFetcher fetcher =
                    new DeadlineVanillaAssetResponseFetcher(client, HEADER_DEADLINE);

            try {
                //when & then
                assertThrows(
                        HttpTimeoutException.class,
                        () -> fetcher.fetch(server.uri(path))
                );
                assertTrue(
                        requestReceived.await(5, TimeUnit.SECONDS),
                        "Server did not receive request"
                );
            } finally {
                release.countDown();
            }
        }
    }

    @Test
    void should_timeout_when_expected_bytes_arrive_without_http_completion() throws Exception {
        //given
        String path = "/pending-completion";
        byte[] expected = {1, 2, 3};

        try (
                HttpClient client = createClient();
                LocalHttpServer server = LocalHttpServer.bind()
        ) {
            server.registerPausedBody(path, 200, expected, SERVER_PAUSE_LIMIT);
            server.start();

            DeadlineVanillaAssetResponseFetcher fetcher =
                    new DeadlineVanillaAssetResponseFetcher(client, BODY_DEADLINE);
            VanillaAssetResponseFetcher.Response response = fetcher.fetch(server.uri(path));

            try (InputStream body = response.body()) {
                //when
                byte[] received = body.readNBytes(expected.length);

                //then
                assertArrayEquals(expected, received);

                //when & then
                assertThrows(HttpTimeoutException.class, body::read);
            }
        }
    }

    @Test
    void should_read_complete_http_response_to_eof() throws Exception {
        //given
        String path = "/complete";
        byte[] expected = {1, 2, 3};

        try (
                HttpClient client = createClient();
                LocalHttpServer server = LocalHttpServer.bind()
        ) {
            server.createContext(path, exchange -> {
                try (exchange) {
                    exchange.sendResponseHeaders(200, expected.length);
                    exchange.getResponseBody().write(expected);
                }
            });

            server.start();

            DeadlineVanillaAssetResponseFetcher fetcher =
                    new DeadlineVanillaAssetResponseFetcher(client, BODY_DEADLINE);

            //when
            VanillaAssetResponseFetcher.Response response = fetcher.fetch(server.uri(path));

            try (InputStream body = response.body()) {
                byte[] received = body.readAllBytes();

                //then
                assertEquals(200, response.statusCode());
                assertArrayEquals(expected, received);
                assertEquals(-1, body.read());
            }
        }
    }

    private static HttpClient createClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    private static void awaitRelease(CountDownLatch release) throws IOException {
        try {
            if (!release.await(SERVER_PAUSE_LIMIT.toNanos(), TimeUnit.NANOSECONDS)) {
                throw new IOException("Test did not release the HTTP response");
            }
        } catch (InterruptedException cause) {
            Thread.currentThread().interrupt();
            throw new IOException("Test HTTP handler was interrupted", cause);
        }
    }
}
