package com.launcher.preparer;

import com.launcher.preparer.model.VanillaAssetIndex;
import com.launcher.preparer.model.VanillaAssetObject;
import com.launcher.preparer.model.VerifiedVanillaAssetObject;
import com.launcher.preparer.verifier.VanillaAssetObjectIntegrityException;
import com.launcher.preparer.verifier.VanillaAssetObjectVerifier;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VanillaAssetObjectsPreparerTest {

    @Test
    void should_return_immutable_verified_objects() throws IOException {
        //given
        byte[] content = bytes("asset-content");
        VanillaAssetObject object = object(content);
        RecordingSource source = new RecordingSource(
                Map.of(
                        object.sha1(), content
                )
        );

        //when
        List<VerifiedVanillaAssetObject> result = preparer(source).prepare(
                new VanillaAssetIndex(
                        List.of(object)
                )
        );

        //then
        assertThrows(
                UnsupportedOperationException.class,
                () -> result.add(
                        new VerifiedVanillaAssetObject(
                                object,
                                hash("SHA-256", content)
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
                        )
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
                )
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
                )
        );

        VanillaAssetObjectsPreparer preparer = preparer(source);

        //when
        List<VerifiedVanillaAssetObject> result = preparer.prepare(
                new VanillaAssetIndex(List.of(first, second))
        );

        //then
        assertEquals(
                List.of(
                        new VerifiedVanillaAssetObject(first, hash("SHA-256", firstBytes)),
                        new VerifiedVanillaAssetObject(second, hash("SHA-256", secondBytes))
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

        private RecordingSource(Map<String, byte[]> contentByHash) {
            this.contentByHash = contentByHash;
        }

        @Override
        public InputStream open(VanillaAssetObject object) throws IOException {
            requested.add(object);

            byte[] content = contentByHash.get(object.sha1());

            if (content == null) {
                throw new IOException("Object unavailable");
            }

            TrackingInputStream stream = new TrackingInputStream(content);
            streams.add(stream);
            return stream;
        }
    }

    private static final class TrackingInputStream extends ByteArrayInputStream {

        private boolean closed;

        private TrackingInputStream(byte[] content) {
            super(content);
        }

        public boolean wasClosed() {
            return closed;
        }
        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }
}
