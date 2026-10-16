package com.launcher.preparer.source;

import com.launcher.preparer.VanillaAssetIndexReader;
import com.launcher.preparer.model.PreparedVanillaAssetIndex;
import com.launcher.preparer.model.VanillaAssetObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpVanillaAssetIndexLoaderTest {

    private static final URI INDEX_URI = URI.create(
            "https://example.test/indexes/17.json?revision=1"
    );

    private static final String OBJECT_HASH = "a".repeat(40);

    @Test
    void should_reject_null_sha1_before_fetching() {
        //given
        HttpVanillaAssetIndexLoader loader = loaderRejectingFetch();

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> loader.load(INDEX_URI, null, 1)
        );
    }

    @Test
    void should_reject_null_uri_before_fetching() {
        //given
        HttpVanillaAssetIndexLoader loader = loaderRejectingFetch();

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> loader.load(null, OBJECT_HASH, 1)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.launcher.preparer.support.InvalidUriProvider#invalidUris")
    void should_reject_invalid_uri_before_fetching(String scenario, URI uri) {
        //given
        HttpVanillaAssetIndexLoader loader = loaderRejectingFetch();

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> loader.load(uri, OBJECT_HASH, 1)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidExpectedHashes")
    void should_reject_invalid_sha1_before_fetching(String expectedSha1) {
        //given
        HttpVanillaAssetIndexLoader loader = loaderRejectingFetch();

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> loader.load(INDEX_URI, expectedSha1, 1)
        );
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 2147483647L, Long.MAX_VALUE})
    void should_reject_unsupported_size_before_fetching(long expectedSize) {
        //given
        HttpVanillaAssetIndexLoader loader = loaderRejectingFetch();

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> loader.load(INDEX_URI, OBJECT_HASH, expectedSize)
        );
    }

    @Test
    void should_propagate_resource_open_failure() {
        //given
        IOException failure = new IOException("Failed to fetch index");
        HttpVanillaAssetIndexLoader loader = loader(uri -> {
            throw failure;
        });

        //when & then
        IOException exception = assertThrows(
                IOException.class,
                () -> loader.load(INDEX_URI, OBJECT_HASH, 1)
        );

        assertSame(failure, exception);
    }

    @Test
    void should_fail_when_resource_close_fails() {
        //given
        byte[] bytes = indexBytes();
        IOException failure = new IOException("Failed to close index");
        TrackingBody body = new TrackingBody(bytes, failure);
        HttpVanillaAssetIndexLoader loader = loader(body);

        //when & then
        IOException exception = assertThrows(
                IOException.class,
                () -> loader.load(
                        INDEX_URI,
                        hash("SHA-1", bytes),
                        bytes.length
                )
        );

        assertSame(failure, exception);
        assertTrue(body.closeCalled);
    }

    @Test
    void should_propagate_read_failure_and_close_body() {
        //given
        IOException failure = new IOException("Failed to read index");
        AtomicBoolean closeCalled = new AtomicBoolean();

        InputStream body = new InputStream() {
            @Override
            public int read() throws IOException {
                throw failure;
            }

            @Override
            public void close() {
                closeCalled.set(true);
            }
        };

        HttpVanillaAssetIndexLoader loader = loader(body);

        //when & then
        IOException exception = assertThrows(
                IOException.class,
                () -> loader.load(INDEX_URI, OBJECT_HASH, 1)
        );

        assertSame(failure, exception);
        assertTrue(closeCalled.get());
    }

    @ParameterizedTest(name = "expected size {0}")
    @ValueSource(ints = {0, 16})
    void should_stop_reading_after_expected_size_plus_one(int expectedSize) {
        //given
        AtomicInteger bytesRead = new AtomicInteger();
        AtomicBoolean closeCalled = new AtomicBoolean();

        InputStream body = new InputStream() {
            @Override
            public int read() {
                int count = bytesRead.incrementAndGet();

                if (count > expectedSize + 1) {
                    throw new AssertionError(
                            "Read more than expectedSize + 1 bytes"
                    );
                }

                return 'a';
            }

            @Override
            public void close() {
                closeCalled.set(true);
            }
        };

        HttpVanillaAssetIndexLoader loader = loader(body);

        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loader.load(INDEX_URI, OBJECT_HASH, expectedSize)
        );

        assertEquals(
                "Asset index size does not match metadata",
                exception.getMessage()
        );
        assertEquals(expectedSize + 1, bytesRead.get());
        assertTrue(closeCalled.get());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidIndexContents")
    void should_reject_invalid_index_and_close_body(
            String scenario,
            byte[] bytes,
            String expectedSha1,
            long expectedSize
    ) {
        //given
        TrackingBody body = new TrackingBody(bytes);
        HttpVanillaAssetIndexLoader loader = loader(body);

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> loader.load(INDEX_URI, expectedSha1, expectedSize)
        );

        assertTrue(body.closeCalled);
    }

    @Test
    void should_return_verified_index_with_source_uri() throws IOException {
        //given
        byte[] bytes = indexBytes();
        TrackingBody body = new TrackingBody(bytes);
        List<URI> requestedUris = new ArrayList<>();

        HttpVanillaAssetIndexLoader loader = loader(uri -> {
            requestedUris.add(uri);
            return new VanillaAssetResponseFetcher.Response(200, body);
        });

        //when
        PreparedVanillaAssetIndex result = loader.load(
                INDEX_URI,
                hash("SHA-1", bytes),
                bytes.length
        );

        //then
        assertEquals(List.of(INDEX_URI), requestedUris);
        assertEquals(INDEX_URI, result.uri());
        assertEquals(bytes.length, result.verified().size());
        assertEquals(
                hash("SHA-256", bytes),
                result.verified().sha256()
        );
        assertEquals(
                List.of(new VanillaAssetObject(OBJECT_HASH, 7)),
                result.verified().index().objects()
        );
        assertTrue(body.closeCalled);
    }

    private static HttpVanillaAssetIndexLoader loader(InputStream body) {
        return loader(uri ->
                new VanillaAssetResponseFetcher.Response(200, body)
        );
    }

    private static HttpVanillaAssetIndexLoader loader(
            VanillaAssetResponseFetcher fetcher
    ) {
        return new HttpVanillaAssetIndexLoader(
                new HttpVanillaAssetResourceSource(fetcher),
                new VanillaAssetIndexReader()
        );
    }

    private static HttpVanillaAssetIndexLoader loaderRejectingFetch() {
        return loader(uri -> {
            throw new AssertionError("Unexpected request: " + uri);
        });
    }

    private static Stream<Arguments> invalidIndexContents() {
        byte[] valid = indexBytes();
        String validSha1 = hash("SHA-1", valid);
        byte[] malformed = """
                {"objects":}
                """.getBytes(StandardCharsets.UTF_8);

        return Stream.of(
                Arguments.of(
                        "SHA-1 mismatch",
                        valid,
                        "0".repeat(40),
                        (long) valid.length
                ),
                Arguments.of(
                        "response shorter than expected",
                        valid,
                        validSha1,
                        (long) valid.length + 1
                ),
                Arguments.of(
                        "response longer than expected",
                        valid,
                        validSha1,
                        (long) valid.length - 1
                ),
                Arguments.of(
                        "malformed JSON with matching metadata",
                        malformed,
                        hash("SHA-1", malformed),
                        (long) malformed.length
                )
        );
    }

    private static String hash(String algorithm, byte[] bytes) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance(algorithm).digest(bytes)
            );
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(
                    algorithm + " must be available in the test runtime",
                    e
            );
        }
    }


    private static Stream<String> invalidExpectedHashes() {
        return Stream.of(
                "a".repeat(39),
                "A".repeat(40),
                "z".repeat(40)
        );
    }

    private static byte[] indexBytes() {
        return """
                {
                    "objects": {
                        "sounds/звук.ogg": {
                            "hash": "%s",
                            "size": 7
                        },
                        "sounds/alias.ogg": {
                            "hash": "%s",
                            "size": 7
                        }
                    }
                }
                """.formatted(OBJECT_HASH, OBJECT_HASH).getBytes(StandardCharsets.UTF_8);
    }

    private static final class TrackingBody extends ByteArrayInputStream {

        private final IOException closeFailure;
        private boolean closeCalled;

        private TrackingBody(byte[] bytes) {
            this(bytes, null);
        }

        private TrackingBody(byte[] bytes, IOException closeFailure) {
            super(bytes);
            this.closeFailure = closeFailure;
        }

        @Override
        public void close() throws IOException {
            closeCalled = true;
            super.close();

            if (closeFailure != null) {
                throw closeFailure;
            }
        }
    }
}
