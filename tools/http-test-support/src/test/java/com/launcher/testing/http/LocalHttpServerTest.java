package com.launcher.testing.http;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(15)
class LocalHttpServerTest {

    private static final Duration WAIT = Duration.ofSeconds(5);
    private static final Duration RELEASE_TIMEOUT = Duration.ofSeconds(30);

    private final HttpClient client = HttpClient.newHttpClient();

    @AfterEach
    void tearDown() throws InterruptedException {
        client.shutdownNow();
        assertTrue(client.awaitTermination(WAIT), "Test HTTP client did not terminate");
    }

    @Test
    void should_release_paused_handler_when_server_is_closed() throws Exception {
        //given
        try (LocalHttpServer server = LocalHttpServer.bind()) {
            PausedHttpResponse response = server.registerPausedBody(
                    "/paused",
                    200,
                    new byte[]{1},
                    RELEASE_TIMEOUT
            );

            server.start();
            CompletableFuture<HttpResponse<byte[]>> exchange = request(server.uri("/paused"));

            try {
                assertTrue(response.awaitStarted(WAIT), "Server did not start the response");
                assertFalse(response.awaitFinished(Duration.ZERO));

                //when
                server.close();

                //then
                assertTrue(response.awaitFinished(WAIT), "Server left the handler paused");
            } finally {
                response.release();
                exchange.cancel(true);
            }
        }
    }

    @Test
    void should_hold_body_until_response_is_released() throws Exception {
        //given
        try (LocalHttpServer server = LocalHttpServer.bind()) {
            PausedHttpResponse response = server.registerPausedBody(
                    "/resource",
                    200,
                    new byte[]{1},
                    new byte[]{2},
                    RELEASE_TIMEOUT
            );

            URI uri = server.uri("/resource");
            server.start();

            //when
            CompletableFuture<HttpResponse<byte[]>> exchange = request(uri);

            assertTrue(response.awaitStarted(WAIT), "Server did not start the response");
            assertFalse(response.awaitFinished(Duration.ofMillis(150)));
            assertFalse(exchange.isDone());

            response.release();
            HttpResponse<byte[]> result = exchange.get(5, TimeUnit.SECONDS);

            //then
            assertEquals("127.0.0.1", uri.getHost());
            assertEquals(200, result.statusCode());
            assertArrayEquals(new byte[]{1, 2}, result.body());
            assertTrue(response.awaitFinished(WAIT));
        }
    }

    private CompletableFuture<HttpResponse<byte[]>> request(URI uri) {
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray());
    }
}
