package com.launcher.preparer.source;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenedVanillaAssetResourceTest {

    @Test
    void should_preserve_uri_and_input() {
        //given
        URI uri = getUri("https://example.org");
        InputStream inputStream = inputStream();

        //when
        OpenedVanillaAssetResource resource = assertDoesNotThrow(
                () -> new OpenedVanillaAssetResource(uri, inputStream)
        );

        //then
        assertEquals(uri, resource.getUri());
        assertSame(inputStream, resource.getInput());
    }

    @Test
    void should_close_input() throws IOException {
        //given
        URI uri = getUri("https://example.org");
        AtomicBoolean closed = new AtomicBoolean();
        InputStream inputStream = inputStream(closed);

        OpenedVanillaAssetResource resource = assertDoesNotThrow(
                () -> new OpenedVanillaAssetResource(uri, inputStream)
        );

        //when
        resource.close();

        //then
        assertTrue(closed.get());
    }

    @Test
    void should_propagate_input_close_failure() {
        //given
        URI uri = getUri("https://example.org");
        AtomicBoolean closed = new AtomicBoolean();
        IOException failure = new IOException("Failed to close stream");
        InputStream inputStream = inputStream(closed, failure);

        OpenedVanillaAssetResource resource = new OpenedVanillaAssetResource(uri, inputStream);

        //when & then
        IOException exception = assertThrows(
                IOException.class,
                resource::close
        );

        assertSame(failure, exception);
        assertFalse(closed.get());
    }

    @ParameterizedTest(name = "should reject invalid resource uri: {0}")
    @MethodSource("invalidUris")
    void should_reject_invalid_resource_uri(
            String cause,
            URI uri
    ) {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new OpenedVanillaAssetResource(uri, inputStream())
        );

        String expectedMessage = "Resource URI must be an absolute HTTPS URI with a " +
                "host and without user info or fragment";

        assertEquals(
                expectedMessage,
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_input() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new OpenedVanillaAssetResource(
                        getUri("https://examle.com"),
                        null
                )
        );

        assertEquals("input", exception.getMessage());
    }

    @Test
    void should_reject_null_uri() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new OpenedVanillaAssetResource(null, inputStream())
        );

        assertEquals("uri", exception.getMessage());
    }

    @Test
    void should_accept_https_uri_with_query() {
        //given
        URI uri = getUri("https://example.org/content-value.jar?query=value");

        //when
        OpenedVanillaAssetResource resource = assertDoesNotThrow(
                () -> new OpenedVanillaAssetResource(uri, inputStream())
        );

        //then
        assertEquals(
                "query=value",
                resource.getUri().getQuery()
        );
    }

    private static Stream<Arguments> invalidUris() {
        return Stream.of(
                Arguments.of(
                        "non absolute uri",
                        getUri("/content/value")
                ),
                Arguments.of(
                        "non https",
                        getUri("http://localhost/content-value.jar")
                ),
                Arguments.of(
                        "without host",
                        getUri("https:/example.org/value.jar")
                ),
                Arguments.of(
                        "with raw user info",
                        getUri("https://admin%40global:p%25ss@example.com")
                ),
                Arguments.of(
                        "with raw fragment",
                        getUri("https://example.com#content%20value")
                )
        );
    }

    private static URI getUri(String value) {
        return URI.create(value);
    }

    private InputStream inputStream() {
        return inputStream(
                new AtomicBoolean(),
                null
        );
    }

    private InputStream inputStream(AtomicBoolean closed) {
        return inputStream(closed, null);
    }

    private InputStream inputStream(AtomicBoolean closed, IOException exception) {
        return new ByteArrayInputStream(new byte[100]) {
            @Override
            public void close() throws IOException {
                if (exception != null) {
                    throw exception;
                }

                closed.set(true);
                super.close();
            }
        };
    }
}
