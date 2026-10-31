package com.launcher.preparer.verifier;

import com.launcher.preparer.model.VanillaAssetObject;
import com.launcher.preparer.model.VerifiedVanillaAssetObject;
import com.launcher.preparer.support.CloseTrackingInputStream;
import com.launcher.preparer.support.TestDigests;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VanillaAssetObjectVerifierTest {

    private final VanillaAssetObjectVerifier verifier =
            new VanillaAssetObjectVerifier();

    @Test
    void should_leave_input_open_for_caller() throws IOException {
        //given
        byte[] bytes = bytes("asset-content");
        VanillaAssetObject expected = expectedObject(bytes);

        //when & then
        try (CloseTrackingInputStream inputStream = new CloseTrackingInputStream(bytes)) {
            verifier.verify(expected, inputStream);

            assertFalse(inputStream.wasCloseCalled());
        }
    }

    @Test
    void should_propagate_failure_when_input_cannot_be_read() {
        //given
        byte[] bytes = bytes("asset-content");
        VanillaAssetObject expected = expectedObject(bytes);
        IOException failure = new IOException("Read failed");

        InputStream inputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw failure;
            }

            @Override
            public int read(byte[] buffer, int offset, int length)
                    throws IOException {
                throw failure;
            }
        };

        //when & then
        IOException actual = assertThrows(
                IOException.class,
                () -> verifier.verify(expected, inputStream)
        );

        assertSame(failure, actual);
    }

    @Test
    void should_reject_sha1_mismatch_when_size_matches() {
        //given
        byte[] bytes = bytes("asset-content");
        VanillaAssetObject expected = new VanillaAssetObject(
                "0".repeat(40),
                bytes.length
        );

        //when & then
        VanillaAssetObjectIntegrityException exception = assertThrows(
                VanillaAssetObjectIntegrityException.class,
                () -> verifier.verify(expected, new ByteArrayInputStream(bytes))
        );

        assertEquals("Asset SHA-1 mismatch", exception.getMessage());
    }

    @Test
    void should_reject_object_smaller_than_expected() {
        //given
        byte[] bytes = bytes("asset-content");
        VanillaAssetObject expected = new VanillaAssetObject(
                TestDigests.sha1(bytes),
                bytes.length + 1L
        );

        //when & then
        VanillaAssetObjectIntegrityException exception = assertThrows(
                VanillaAssetObjectIntegrityException.class,
                () -> verifier.verify(expected, new ByteArrayInputStream(bytes))
        );

        assertEquals("Asset size mismatch", exception.getMessage());
    }

    @Test
    void should_reject_object_larger_than_expected() {
        //given
        byte[] bytes = bytes("asset-content");
        VanillaAssetObject expected = new VanillaAssetObject(
                TestDigests.sha1(bytes),
                bytes.length - 1L
        );

        //when & then
        VanillaAssetObjectIntegrityException exception = assertThrows(
                VanillaAssetObjectIntegrityException.class,
                () -> verifier.verify(expected, new ByteArrayInputStream(bytes))
        );

        assertEquals("Asset size mismatch", exception.getMessage());
    }

    @Test
    void should_return_sha256_when_object_bytes_match() throws IOException {
        //given
        byte[] bytes = new byte[20_000];
        Arrays.fill(bytes, (byte) 7);

        VanillaAssetObject expected = expectedObject(bytes);

        //when
        VerifiedVanillaAssetObject result = verifier.verify(
                expected, new ByteArrayInputStream(bytes)
        );

        //then
        assertEquals(expected, result.object());
        assertEquals(TestDigests.sha256(bytes), result.sha256());
    }

    private static VanillaAssetObject expectedObject(byte[] bytes) {
        return new VanillaAssetObject(
                TestDigests.sha1(bytes),
                bytes.length
        );
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
