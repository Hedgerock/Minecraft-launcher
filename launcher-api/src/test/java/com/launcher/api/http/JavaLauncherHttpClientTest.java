package com.launcher.api.http;

import com.launcher.api.http.exception.HttpRequestException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaLauncherHttpClientTest {
    private JavaLauncherHttpClient javaLauncherHttpClient;
    private HttpServer httpServer;
    private static final String EXPECTED_BODY = """
            {"version": "1.12/2"}
            """;

    @BeforeEach
    void setUp() throws IOException {
        httpServer = HttpServer.create(
                new InetSocketAddress(0),
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
                () -> javaLauncherHttpClient.get(uri("/manifest.json"))
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
                () -> javaLauncherHttpClient.getBytes(uri("/manifest.json"))
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
                () -> javaLauncherHttpClient.getBytes(uri("/manifest.json"))
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
                () -> javaLauncherHttpClient.get(uri("/manifest.json"))
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
        byte[] result = javaLauncherHttpClient.getBytes(uri("/manifest.json"));

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
        String result = javaLauncherHttpClient.get(uri("/manifest.json"));

        //then
        assertEquals(EXPECTED_BODY, result);
    }

    @Test
    void should_reject_null_uri_for_byte_result() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> javaLauncherHttpClient.getBytes(null)
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
                () -> javaLauncherHttpClient.get(null)
        );

        assertEquals(
                "uri",
                exception.getMessage()
        );
    }


    private URI uri(String path) {
        return URI.create(
                "http://localhost:" +
                        httpServer.getAddress().getPort() +
                        path
        );
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
        httpServer.createContext(
                path,
                exchange -> {
                    if (location != null) {
                        exchange.getResponseHeaders().set("Location", location);
                    }

                    exchange.sendResponseHeaders(
                            statusCode,
                            body.length
                    );

                    exchange.getResponseBody().write(body);
                    exchange.close();
                }
        );
    }
}
