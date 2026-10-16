package com.launcher.preparer.source;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpVanillaAssetResourceSourceTest {

    @Test
    void should_preserve_status_failure_when_body_close_fails() {
        //given
        IOException closeFailure = new IOException("Failed to close body");
        InputStream inputStream = new ByteArrayInputStream(new byte[100]) {
            @Override
            public void close() throws IOException {
                throw closeFailure;
            }
        };

        VanillaAssetResponseFetcher fetcher = uri -> response(404, inputStream);

        URI uri = URI.create("https://example.test/source-a?revision=1");
        HttpVanillaAssetResourceSource source = new HttpVanillaAssetResourceSource(fetcher);

        //when & then
        IOException exception = assertThrows(
                IOException.class,
                () -> source.open(uri)
        );

        assertEquals(1, exception.getSuppressed().length);
        assertSame(closeFailure, exception.getSuppressed()[0]);
        assertEquals("Unexpected vanilla asset HTTP status: 404", exception.getMessage());
    }

    @Test
    void should_restore_interrupt_flag_when_request_is_interrupted() {
        //given
        InterruptedException interruptionFailure =
                new InterruptedException("Fetch was interrupted");
        VanillaAssetResponseFetcher fetcher = uri -> {
            throw interruptionFailure;
        };

        URI uri = URI.create("https://example.test/source-a?revision=1");
        HttpVanillaAssetResourceSource source = new HttpVanillaAssetResourceSource(fetcher);

        try {
            //when & then
            IOException exception = assertThrows(
                    IOException.class,
                    () -> source.open(uri)
            );

            assertInstanceOf(
                    InterruptedException.class,
                    exception.getCause()
            );
        } finally {
            assertTrue(Thread.interrupted());
        }
    }

    @Test
    void should_propagate_fetch_failure() {
        //given
        IOException failure = new IOException("Failed to fetch");
        VanillaAssetResponseFetcher fetcher = uri -> {
            throw failure;
        };

        URI uri = URI.create("https://example.test/source-a?revision=1");
        HttpVanillaAssetResourceSource source = new HttpVanillaAssetResourceSource(fetcher);

        //when & then
        IOException exception = assertThrows(
                IOException.class,
                () -> source.open(uri)
        );

        assertSame(failure, exception);
    }

    @ParameterizedTest(name = "should reject status {0}")
    @ValueSource(ints = { 206, 301, 302, 307, 308, 404, 500 })
    void should_reject_non_success_status_and_close_body(int statusCode) {
        //given
        AtomicBoolean hasClosed = new AtomicBoolean(false);

        InputStream inputStream = new ByteArrayInputStream(new byte[0]) {
            @Override
            public void close() throws IOException {
                hasClosed.set(true);
                super.close();
            }
        };

        VanillaAssetResponseFetcher fetcher = uri -> response(statusCode, inputStream);

        URI uri = URI.create("https://example.test/source-a?revision=1");
        HttpVanillaAssetResourceSource source = new HttpVanillaAssetResourceSource(fetcher);

        //when & then
        IOException exception = assertThrows(
                IOException.class,
                () -> source.open(uri)
        );

        assertEquals("Unexpected vanilla asset HTTP status: " + statusCode, exception.getMessage());
        assertTrue(hasClosed.get());
    }

    @Test
    void should_reject_invalid_uri_before_fetching() {
        //given
        AtomicBoolean fetchRequested = new AtomicBoolean();
        VanillaAssetResponseFetcher fetcher = uri -> {
            fetchRequested.set(true);
            return response(200, new ByteArrayInputStream(new byte[0]));
        };

        URI uri = URI.create("http://example.test/source-a?revision=1");
        HttpVanillaAssetResourceSource source = new HttpVanillaAssetResourceSource(fetcher);

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> source.open(uri)
        );

        assertFalse(fetchRequested.get());
    }

    @Test
    void should_return_resource_with_requested_uri_and_response_body() throws IOException {
        //given
        AtomicBoolean closed = new AtomicBoolean();

        InputStream inputStream = new ByteArrayInputStream(new byte[100]) {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        };

        List<URI> requestedUris = new ArrayList<>();
        VanillaAssetResponseFetcher fetcher = uri -> {
            requestedUris.add(uri);
            return response(200, inputStream);
        };
        URI uri = URI.create("https://example.test/source-a?revision=1");
        HttpVanillaAssetResourceSource source = new HttpVanillaAssetResourceSource(fetcher);

        //when
        OpenedVanillaAssetResource resource = source.open(uri);

        //then
        assertEquals(uri, resource.getUri());
        assertSame(inputStream, resource.getInput());
        assertEquals(
                List.of(uri),
                requestedUris
        );

        assertFalse(closed.get(), "InputStream must remain open after open()");

        //when
        resource.close();

        //then
        assertTrue(closed.get(), "InputStream must be closed after resource.close()");
    }

    private VanillaAssetResponseFetcher.Response response(
            int statusCode,
            InputStream inputStream
    ) {
        return new VanillaAssetResponseFetcher.Response(statusCode, inputStream);
    }
}
