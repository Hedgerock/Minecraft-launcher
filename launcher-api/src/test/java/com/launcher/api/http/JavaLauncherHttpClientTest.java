package com.launcher.api.http;

import com.launcher.api.http.exception.HttpRequestException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaLauncherHttpClientTest {
    private static final HttpRequestLimits DEFAULT_LIMITS =
            new HttpRequestLimits(Duration.ofSeconds(5), 1024);

    private static final HttpRequestLimits DEADLINE_LIMITS =
            new HttpRequestLimits(Duration.ofSeconds(3), 1024);

    private JavaLauncherHttpClient javaLauncherHttpClient;
    private HttpServer httpServer;
    private static final String EXPECTED_BODY = """
            {"version": "1.12/2"}
            """;

    @BeforeEach
    void setUp() throws IOException {
        httpServer = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0),
                0
        );

        httpServer.start();

        javaLauncherHttpClient = new JavaLauncherHttpClient();
    }

    @AfterEach
    void tearDown() {
        httpServer.stop(0);
    }

    @Test
    void should_restore_interrupt_flag_when_waiting_for_response_is_interrupted() throws Exception {
        //given
        String path = "/interrupted";
        CountDownLatch prefixSent = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        HttpRequestLimits limits = new HttpRequestLimits(Duration.ofSeconds(30), 1024);
        registerPausedBody(path, 200, new byte[]{1}, prefixSent, release);

        FutureTask<InterruptedCall> call = new FutureTask<>(() -> {
            HttpRequestException failure = assertThrows(
                    HttpRequestException.class,
                    () -> javaLauncherHttpClient.getBytes(uri(path), limits)
            );

            return new InterruptedCall(failure, Thread.currentThread().isInterrupted());
        });

        //when
        Thread worker = Thread.ofPlatform().daemon().start(call);

        try {
            assertTrue(prefixSent.await(5, TimeUnit.SECONDS), "Server did not send the prefix");
            worker.interrupt();
            InterruptedCall result = call.get(5, TimeUnit.SECONDS);

            //then
            assertInstanceOf(InterruptedException.class, result.failure().getCause());
            assertTrue(result.interrupted(), "Client must restore the interrupt flag");
        } finally {
            release.countDown();
            worker.interrupt();
            worker.join(5_000);
        }
    }

    @Test
    void should_time_out_when_response_headers_are_not_sent() {
        //given
        String path = "/pending-headers";
        CountDownLatch requestReceived = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        httpServer.createContext(path, exchange -> {
           try (exchange) {
               requestReceived.countDown();
               awaitResponseRelease(release);
               exchange.sendResponseHeaders(200, -1);
           }
        });

        try {
            //when & then
            HttpRequestException failure = assertThrows(
                    HttpRequestException.class,
                    () -> javaLauncherHttpClient.getBytes(uri(path), DEADLINE_LIMITS)
            );

            assertEquals(0L, requestReceived.getCount(), "Server never received the request");
            assertInstanceOf(TimeoutException.class, failure.getCause());
        } finally {
            release.countDown();
        }
    }

    @Test
    void should_cancel_exchange_when_successful_body_stops_arriving()
            throws InterruptedException {
        //given
        String path = "/pending-body";
        CountDownLatch prefixSent = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        registerPausedBody(path, 200, new byte[]{1}, prefixSent, release);

        HttpClient transport = HttpClient.newHttpClient();
        JavaLauncherHttpClient client = new JavaLauncherHttpClient(transport);

        try {
            //when
            HttpRequestException failure = assertThrows(
                    HttpRequestException.class,
                    () -> client.getBytes(uri(path), DEADLINE_LIMITS)
            );

            //then
            assertEquals(0L, prefixSent.getCount(), "Server never sent the first body chunk");
            assertInstanceOf(TimeoutException.class, failure.getCause());

            transport.shutdown();
            assertTrue(
                    transport.awaitTermination(Duration.ofSeconds(5)),
                    "HTTP exchange must terminate while the server still holds the body open"
            );
        } finally {
            release.countDown();
            transport.shutdownNow();
        }
    }

    @Test
    void should_reject_status_without_waiting_for_response_body() {
        //given
        String path = "/missing";
        CountDownLatch release = new CountDownLatch(1);
        registerPausedBody(path, 404, new byte[0], new CountDownLatch(1), release);

        try {
            //when & then
            HttpRequestException failure = assertThrows(
                    HttpRequestException.class,
                    () -> javaLauncherHttpClient.getBytes(uri(path), DEFAULT_LIMITS)
            );

            assertEquals("HTTP GET failed with status code: 404", failure.getMessage());
        } finally {
            release.countDown();
        }
    }

    @Test
    void should_decode_text_using_response_charset() {
        //given
        String path = "/encoded";
        String expected = "caf\u00e9";
        byte[] content = expected.getBytes(StandardCharsets.ISO_8859_1);

        httpServer.createContext(path, exchange -> {
           try (exchange) {
               exchange.getResponseHeaders()
                       .set("Content-Type", "text/plain; charset=ISO-8859-1");
               exchange.sendResponseHeaders(200, content.length);
               exchange.getResponseBody().write(content);
           }
        });

        //when
        String result = javaLauncherHttpClient.get(uri(path), DEFAULT_LIMITS);

        //then
        assertEquals(expected, result);
    }

    @Test
    void should_reject_chunked_body_when_actual_bytes_exceed_limit() {
        //given
        String path = "/chunked";
        byte[] content = "\u00e9\u00e9\u00e9".getBytes(StandardCharsets.UTF_8);
        HttpRequestLimits limits = new HttpRequestLimits(Duration.ofSeconds(5), 4);

        httpServer.createContext(path, exchange -> {
            try (exchange) {
                exchange.sendResponseHeaders(200, 0);
                exchange.getResponseBody().write(content);
            }
        });

        //when & then
        HttpRequestException failure = assertThrows(
                HttpRequestException.class,
                () -> javaLauncherHttpClient.get(uri(path), limits)
        );

        assertEquals("HTTP response body exceeds 4 bytes", failure.getMessage());
    }

    @Test
    void should_reject_get_redirection() {
        //given
        registerResponse(
                "/manifest.json",
                302,
                EXPECTED_BODY,
                "/manifest-redirect.json"
        );

        //when & then
        HttpRequestException exception = assertThrows(
                HttpRequestException.class,
                () -> javaLauncherHttpClient.get(uri("/manifest.json"), DEFAULT_LIMITS)
        );

        assertEquals(
                "HTTP GET failed with status code: 302",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_get_bytes_redirection() {
        //given
        registerResponse(
                "/manifest.json",
                302,
                new byte[0],
                "/manifest-redirect.json"
        );

        //when & then
        HttpRequestException exception = assertThrows(
                HttpRequestException.class,
                () -> javaLauncherHttpClient.getBytes(uri("/manifest.json"), DEFAULT_LIMITS)
        );

        assertEquals(
                "HTTP GET failed with status code: 302",
                exception.getMessage()
        );
    }

    @Test
    void should_throw_exception_for_byte_result_when_status_not_success() {
        //given
        registerResponse(
                "/manifest.json",
                404,
                ""
        );

        //when & then
        HttpRequestException exception = assertThrows(
                HttpRequestException.class,
                () -> javaLauncherHttpClient.getBytes(uri("/manifest.json"), DEFAULT_LIMITS)
        );

        assertTrue(exception.getMessage().contains("HTTP GET failed with status code"));
    }

    @Test
    void should_throw_exception_for_non_success_status() {
        //given
        registerResponse(
                "/manifest.json",
                404,
                ""
        );

        //when & then
        HttpRequestException exception = assertThrows(
                HttpRequestException.class,
                () -> javaLauncherHttpClient.get(uri("/manifest.json"), DEFAULT_LIMITS)
        );

        assertTrue(exception.getMessage().contains("HTTP GET failed with status code"));
    }

    @Test
    void should_return_response_body_as_bytes_for_successful_get() {
        //given
        byte[] expectedBody = {(byte) 0xFF, 0x00, 0x41};

        registerResponse(
                "/manifest.json",
                200,
                expectedBody
        );

        //when
        byte[] result = javaLauncherHttpClient.getBytes(uri("/manifest.json"), DEFAULT_LIMITS);

        //then
        assertArrayEquals(
                expectedBody,
                result
        );
    }

    @Test
    void should_return_response_body_for_successful_get() {
        //given
        registerResponse(
                "/manifest.json",
                200,
                EXPECTED_BODY
        );

        //when
        String result = javaLauncherHttpClient.get(uri("/manifest.json"), DEFAULT_LIMITS);

        //then
        assertEquals(EXPECTED_BODY, result);
    }

    @Test
    void should_reject_null_uri_for_byte_result() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> javaLauncherHttpClient.getBytes(null, DEFAULT_LIMITS)
        );

        assertEquals(
                "uri",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_uri() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> javaLauncherHttpClient.get(null, DEFAULT_LIMITS)
        );

        assertEquals(
                "uri",
                exception.getMessage()
        );
    }


    private URI uri(String path) {
        return URI.create("http://127.0.0.1:" + httpServer.getAddress().getPort() + path);
    }

    private void registerResponse(
            String path,
            int statusCode,
            String body
    ) {
        registerResponse(
                path,
                statusCode,
                body.getBytes(StandardCharsets.UTF_8),
                null
        );
    }

    private void registerResponse(
            String path,
            int statusCode,
            String body,
            String location
    ) {
        registerResponse(
                path,
                statusCode,
                body.getBytes(StandardCharsets.UTF_8),
                location
        );
    }

    private void registerResponse(
            String path,
            int statusCode,
            byte[] body
    ) {
       registerResponse(
               path,
               statusCode,
               body,
               null
       );
    }

    private void registerResponse(
            String path,
            int statusCode,
            byte[] body,
            String location
    ) {
        httpServer.createContext(path, exchange -> {
            try (exchange) {
                if (location != null) {
                    exchange.getResponseHeaders().set("Location", location);
                }

                exchange.sendResponseHeaders(statusCode, body.length);
                exchange.getResponseBody().write(body);
            }
        });
    }

    private void registerPausedBody(
            String path,
            int status,
            byte[] prefix,
            CountDownLatch prefixSent,
            CountDownLatch release
    ) {
        httpServer.createContext(path, exchange -> {
           try (exchange) {
               exchange.sendResponseHeaders(status, 0);

               if (prefix.length > 0) {
                   OutputStream body = exchange.getResponseBody();
                   body.write(prefix);
                   body.flush();
               }

               prefixSent.countDown();
               awaitResponseRelease(release);
           }
        });
    }

    private static void awaitResponseRelease(CountDownLatch release) throws IOException {
        try {
            if (!release.await(15, TimeUnit.SECONDS)) {
                throw new IOException("Test did not release the response");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Test response handler interrupted", e);
        }
    }

    private record InterruptedCall(HttpRequestException failure, boolean interrupted) {
    }
}
