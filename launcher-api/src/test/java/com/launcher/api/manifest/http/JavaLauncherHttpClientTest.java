package com.launcher.api.manifest.http;

import com.launcher.api.http.JavaLauncherHttpClient;
import com.launcher.api.http.exception.HttpRequestException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

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
    void should_reject_null_uri() {
        //when & then
        NullPointerException nullPointerException = assertThrows(
                NullPointerException.class,
                () -> javaLauncherHttpClient.get(null)
        );

        assertTrue(nullPointerException.getMessage().contains("uri"));
    }

    @SuppressWarnings("SameParameterValue")
    private URI uri(String path) {
        return URI.create(
                "http://localhost:" +
                        httpServer.getAddress().getPort() +
                        path
        );
    }

    @SuppressWarnings("SameParameterValue")
    private void registerResponse(
            String path,
            int statusCode,
            String body
    ) {
        httpServer.createContext(
                path,
                exchange -> {
                    byte[] response = body.getBytes(StandardCharsets.UTF_8);

                    exchange.sendResponseHeaders(
                            statusCode,
                            response.length
                    );

                    exchange.getResponseBody().write(response);
                    exchange.close();
                }
        );
    }
}
