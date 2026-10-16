package com.launcher.preparer;

import com.launcher.preparer.model.PreparedVanillaAssetObject;
import com.launcher.preparer.model.VanillaAssetIndex;
import com.launcher.preparer.model.VanillaAssetObject;
import com.launcher.preparer.model.VerifiedVanillaAssetObject;
import com.launcher.preparer.source.OpenedVanillaAssetResource;
import com.launcher.preparer.verifier.VanillaAssetObjectIntegrityException;
import com.launcher.preparer.verifier.VanillaAssetObjectVerifier;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VanillaAssetObjectsPreparerTest {

    @Test
    void should_stop_when_resource_close_fails() {
        //given
        byte[] firstBytes = bytes("first-content");
        byte[] secondBytes = bytes("second-content");
        VanillaAssetObject first = object(firstBytes);
        VanillaAssetObject second = object(secondBytes);
        IOException failure = new IOException("Failed to close");

        RecordingSource source = new RecordingSource(
                Map.of(
                        first.sha1(), firstBytes,
                        second.sha1(), secondBytes
                ),
                Map.of(),
                failure
        );

        VanillaAssetObjectsPreparer preparer = preparer(source);

        //when & then
        IOException exception = assertThrows(
                IOException.class,
                () -> preparer.prepare(
                        new VanillaAssetIndex(List.of(first, second))
                )
        );

        assertSame(failure, exception);

        assertEquals(
                List.of(
                        first
                ),
                source.requested
        );
    }

    @Test
    void should_return_immutable_verified_objects() throws IOException {
        //given
        byte[] content = bytes("asset-content");
        VanillaAssetObject object = object(content);
        RecordingSource source = new RecordingSource(
                Map.of(
                        object.sha1(), content
                ),
                Map.of()
        );

        //when
        List<PreparedVanillaAssetObject> result = preparer(source).prepare(
                new VanillaAssetIndex(
                        List.of(object)
                )
        );

        //then
        assertThrows(
                UnsupportedOperationException.class,
                () -> result.add(
                        preparedVanillaAssetObject(
                                new VerifiedVanillaAssetObject(
                                        object,
                                        hash("SHA-256", content)
                                ),
                                "https://example.test/source-a?revision=1"
                        )
                )
        );
    }

    @Test
    void should_stop_when_source_cannot_open_an_object() {
        //given
        byte[] firstBytes = bytes("first-content");
        byte[] secondBytes = bytes("second-content");
        byte[] thirdBytes = bytes("third-content");

        VanillaAssetObject first = object(firstBytes);
        VanillaAssetObject second = object(secondBytes);
        VanillaAssetObject third = object(thirdBytes);

        RecordingSource source =
                new RecordingSource(
                        Map.of(
                                first.sha1(), firstBytes,
                                third.sha1(), thirdBytes
                        ),
                        Map.of()
                );

        VanillaAssetObjectsPreparer preparer = preparer(source);
        VanillaAssetIndex index = new VanillaAssetIndex(List.of(first, second, third));

        //when & then
        IOException exception = assertThrows(
                IOException.class,
                () -> preparer.prepare(index)
        );

        assertEquals(
                "Object unavailable",
                exception.getMessage()
        );

        assertEquals(List.of(first, second), source.requested);
        assertTrue(source.streams.getFirst().wasClosed());
    }

    @Test
    void should_stop_and_close_streams_when_object_is_corrupt() {
        //given
        byte[] firstBytes = bytes("first-content");
        byte[] secondBytes = bytes("second-content");
        byte[] thirdBytes = bytes("third-content");
        byte[] corruptedSecondBytes = secondBytes.clone();
        corruptedSecondBytes[0] ^= 1;

        VanillaAssetObject first = object(firstBytes);
        VanillaAssetObject second = object(secondBytes);
        VanillaAssetObject third = object(thirdBytes);

        RecordingSource source = new RecordingSource(
                Map.of(
                        first.sha1(), firstBytes,
                        second.sha1(), corruptedSecondBytes,
                        third.sha1(), thirdBytes
                ),
                Map.of()
        );

        VanillaAssetObjectsPreparer preparer = preparer(source);
        VanillaAssetIndex index = new VanillaAssetIndex(
                List.of(first, second, third)
        );

        //when & then
        assertThrows(
                VanillaAssetObjectIntegrityException.class,
                () -> preparer.prepare(index)
        );

        assertEquals(
                List.of(first, second),
                source.requested
        );

        assertTrue(source.streams.stream().allMatch(TrackingInputStream::wasClosed));
    }

    @Test
    void should_verify_all_objects_and_close_their_streams() throws IOException {
        //given
        byte[] firstBytes = bytes("first-content");
        byte[] secondBytes = bytes("second-content");
        VanillaAssetObject first = object(firstBytes);
        VanillaAssetObject second = object(secondBytes);

        RecordingSource source = new RecordingSource(
                Map.of(
                        first.sha1(), firstBytes,
                        second.sha1(), secondBytes
                ),
                Map.of(
                        first, URI.create("https://example.test/source-a?revision=1"),
                        second, URI.create("https://example.test/source-b?revision=2")
                )
        );

        VanillaAssetObjectsPreparer preparer = preparer(source);

        //when
        List<PreparedVanillaAssetObject> result = preparer.prepare(
                new VanillaAssetIndex(List.of(first, second))
        );

        //then
        assertEquals(
                List.of(
                        preparedVanillaAssetObject(
                                new VerifiedVanillaAssetObject(
                                        first, hash("SHA-256", firstBytes)
                                ),
                                "https://example.test/source-a?revision=1"
                        ),
                        preparedVanillaAssetObject(
                                new VerifiedVanillaAssetObject(
                                        second, hash("SHA-256", secondBytes)
                                ),
                                "https://example.test/source-b?revision=2"
                        )
                ),
                result
        );

        assertEquals(
                List.of(first, second),
                source.requested
        );

        assertTrue(source.streams.stream().allMatch(TrackingInputStream::wasClosed));
    }

    private static VanillaAssetObjectsPreparer preparer(RecordingSource recordingSource) {
        return new VanillaAssetObjectsPreparer(
                recordingSource,
                new VanillaAssetObjectVerifier()
        );
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private static VanillaAssetObject object(byte[] content) {
        return new VanillaAssetObject(
                hash("SHA-1", content),
                content.length
        );
    }

    private static String hash(String algorithm, byte[] content) {
        try {
            byte[] digest = MessageDigest.getInstance(algorithm).digest(content);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(
                    algorithm + " must be available",
                    e
            );
        }
    }

    private static final class RecordingSource implements VanillaAssetObjectSource {

        private final Map<String, byte[]> contentByHash;
        private final List<VanillaAssetObject> requested = new ArrayList<>();
        private final List<TrackingInputStream> streams = new ArrayList<>();
        private final Map<VanillaAssetObject, URI> urls;
        private IOException closeException;

        private RecordingSource(
                Map<String, byte[]> contentByHash,
                Map<VanillaAssetObject, URI> urls
        ) {
            this.contentByHash = contentByHash;
            this.urls = urls;
        }

        public RecordingSource(
                Map<String, byte[]> contentByHash,
                Map<VanillaAssetObject, URI> urls,
                IOException closeException
        ) {
            this.contentByHash = contentByHash;
            this.urls = urls;
            this.closeException = closeException;
        }

        @Override
        public OpenedVanillaAssetResource open(VanillaAssetObject object) throws IOException {
            requested.add(object);

            byte[] content = contentByHash.get(object.sha1());

            if (content == null) {
                throw new IOException("Object unavailable");
            }

            TrackingInputStream stream = new TrackingInputStream(content, closeException);
            streams.add(stream);
            URI uri = urls.isEmpty()
                    ? URI.create("https://example.test/source-a?revision=1")
                    : urls.get(object);

            return new OpenedVanillaAssetResource(
                    uri,
                    stream
            );
        }
    }

    private PreparedVanillaAssetObject preparedVanillaAssetObject(
            VerifiedVanillaAssetObject object,
            String url
    ) {
        return new PreparedVanillaAssetObject(
                object,
                URI.create(url)
        );
    }

    private static final class TrackingInputStream extends ByteArrayInputStream {

        private boolean closed;
        private final IOException closeException;

        private TrackingInputStream(byte[] content, IOException closeException) {
            super(content);
            this.closeException = closeException;
        }

        public boolean wasClosed() {
            return closed;
        }
        @Override
        public void close() throws IOException {
            if (closeException != null) {
                throw closeException;
            }

            closed = true;
            super.close();
        }
    }
}
