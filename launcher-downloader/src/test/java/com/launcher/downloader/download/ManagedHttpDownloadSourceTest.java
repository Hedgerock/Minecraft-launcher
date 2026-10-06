package com.launcher.downloader.download;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManagedHttpDownloadSourceTest {
    private final ManagedHttpDownloadSource managedHttpDownloadSource =
            new ManagedHttpDownloadSource();

    @Test
    void should_reject_redirect_and_close_response_body() {
        //given
        AtomicBoolean bodyClosed = new AtomicBoolean();
        AtomicInteger fetchCalls = new AtomicInteger();

        InputStream body = new ByteArrayInputStream(
                new byte[0]
        ) {
            @Override
            public void close() throws IOException {
                bodyClosed.set(true);
                super.close();
            }
        };

        ResponseFetcher fetcher = uri -> {
            fetchCalls.incrementAndGet();
            return new ResponseFetcher.Response(302, body);
        };

        ManagedHttpDownloadSource source = new ManagedHttpDownloadSource(fetcher);

        //when
        IOException exception = assertThrows(
                IOException.class,
                () -> source.open("https://example.org/resource.jar")
        );

        //then
        assertEquals(
                "Unexpected managed resource HTTP status: 302",
                exception.getMessage()
        );

        assertEquals(1, fetchCalls.get());
        assertTrue(bodyClosed.get());
    }

    @Test
    void should_return_body_value() throws IOException {
        //given
        byte[] expectedResult = "ExpectedValue".getBytes(StandardCharsets.UTF_8);
        InputStream body = new ByteArrayInputStream(expectedResult);

        ResponseFetcher fetcher = uri -> new ResponseFetcher.Response(
                200,
                body
        );

        ManagedHttpDownloadSource source = new ManagedHttpDownloadSource(fetcher);

        //when
        try (InputStream result = source.open("https://example.org")) {
            //then
            assertArrayEquals(expectedResult, result.readAllBytes());
        }
    }

    @Test
    void should_reject_not_success_status() {
        //given
        ResponseFetcher fetcher = uri -> new ResponseFetcher.Response(
                404,
                new ByteArrayInputStream(new byte[0])
        );

        ManagedHttpDownloadSource source = new ManagedHttpDownloadSource(fetcher);

        //when & then
        IOException exception = assertThrows(
                IOException.class,
                () -> source.open("https://example.org")
        );

        assertEquals(
                "Unexpected managed resource HTTP status: 404",
                exception.getMessage()
        );
    }

    @Test
    void should_interrupt_when_fetcher_failed() {
        //given
        ResponseFetcher failingResponseFetcher = uri -> {
            throw new InterruptedException("Failed to fetch");
        };

        ManagedHttpDownloadSource source = new ManagedHttpDownloadSource(failingResponseFetcher);

        //when & then
        try {
            IOException exception = assertThrows(
                    IOException.class,
                    () -> source.open("https://example.org")
            );

            assertEquals(
                    "Managed resource download interrupted",
                    exception.getMessage()
            );

            assertInstanceOf(
                    InterruptedException.class,
                    exception.getCause()
            );
        } finally {
            assertTrue(Thread.currentThread().isInterrupted());
            Thread.interrupted();
        }
    }

    @Test
    void should_reject_invalid_uri() {
        //given
        String invalidUri = "https://example. org";

        //when & then
        IOException exception = assertThrows(
                IOException.class,
                () -> managedHttpDownloadSource.open(invalidUri)
        );

        assertEquals("Invalid managed resource URI", exception.getMessage());
    }

    @Test
    void should_reject_non_https_uri() {
        //given
        List<String> invalidUris = List.of(
                "ftp://example.org",
                "http://localhost:3000"
        );

        String expectedMessage = "Managed resource requires an HTTPS URI";

        //when & then
        invalidUris.forEach(uri -> {
            IOException exception = assertThrows(
                   IOException.class,
                   () -> managedHttpDownloadSource.open(uri)
           );

           assertEquals(expectedMessage, exception.getMessage());
        });
    }

    @Test
    void should_reject_null_host() {
        //when & then
        IOException exception = assertThrows(
                IOException.class,
                () -> managedHttpDownloadSource.open(
                        "https:/example.org"
                )
        );

        assertEquals(
                "Managed resource requires an HTTPS URI",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_uri() {
        //when & then
        IOException exception = assertThrows(
                IOException.class,
                () -> managedHttpDownloadSource.open(null)
        );

        assertEquals(
                "Invalid managed resource URI",
                exception.getMessage()
        );
    }
}
