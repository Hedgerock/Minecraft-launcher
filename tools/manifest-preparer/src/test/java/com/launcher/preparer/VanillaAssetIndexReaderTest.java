package com.launcher.preparer;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VanillaAssetIndexReaderTest {

    private static final String FIRST_HASH = "a".repeat(40);
    private static final String SECOND_HASH = "b".repeat(40);

    private final VanillaAssetIndexReader reader =
            new VanillaAssetIndexReader();

    @Test
    void should_reject_duplicate_logical_name() {
        //given
        String json = getJsonWithDuplicateLogicalNames();

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> readMatchingMetadata(json)
        );
    }

    @Test
    void should_reject_malformed_json() {
        //given
        String json = """
                    {"objects":}
                """;

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> readMatchingMetadata(json)
        );
    }

    @Test
    void should_reject_index_sha1_that_differs_from_metadata() {
        //given
        byte[] bytes = """
                    {"objects": {}}
                """.getBytes(StandardCharsets.UTF_8);

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> reader.read(bytes, "0".repeat(40), bytes.length)
        );
    }

    @Test
    void should_reject_index_size_that_differs_from_metadata() {
        //given
        byte[] bytes = """
                    {"objects": {}}
                """.getBytes(StandardCharsets.UTF_8);

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> reader.read(bytes, sha1(bytes), bytes.length + 1)
        );
    }

    @Test
    void should_reject_fractional_object_size() {
        //given
        String json = getJsonWithFractionalSize();

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> readMatchingMetadata(json)
        );
    }

    @Test
    void should_reject_conflicting_sizes_for_shared_hash() {
        //given
        String json = getConflictingJson();

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> readMatchingMetadata(json)
        );
    }

    @Test
    void should_read_unique_objects_and_deduplicate_shared_hash() {
        //given
        String json = getJson();

        //when
        VanillaAssetIndex result = readMatchingMetadata(json);

        //then
        assertEquals(
                List.of(
                        new VanillaAssetObject(FIRST_HASH, 10),
                        new VanillaAssetObject(SECOND_HASH, 20)
                ),
                result.objects()
        );
    }

    private String getJsonWithDuplicateLogicalNames() {
        return """
                {
                    "objects": {
                        "same-name": {"hash": "%s", "size": 10},
                        "same-name": {"hash": "%s", "size": 10}
                    }
                }
                """.formatted(FIRST_HASH, FIRST_HASH);
    }

    private String getJsonWithFractionalSize() {
        return """
                {
                    "objects": {
                        "first": {"hash": "%s", "size": 10.5}
                    }
                }
                """.formatted(FIRST_HASH);
    }

    private String getConflictingJson() {
        return """
                {
                    "objects": {
                        "first": {"hash": "%s", "size": 10},
                        "second": {"hash": "%s", "size": 20}
                    }
                }
                """.formatted(FIRST_HASH, FIRST_HASH);
    }

    private String getJson() {
        return """
                {
                    "objects": {
                        "sounds/first.ogg": {"hash": "%s", "size": 10},
                        "sounds/alias.ogg": {"hash": "%s", "size": 10},
                        "sounds/second.ogg": {"hash": "%s", "size": 20}
                    }
                }
                """.formatted(FIRST_HASH, FIRST_HASH, SECOND_HASH);
    }

    private VanillaAssetIndex readMatchingMetadata(String json) {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return reader.read(bytes, sha1(bytes), bytes.length);
    }

    private String sha1(byte[] bytes) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-1").digest(bytes);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(
                    "SHA-1 must be available in the test runtime",
                    e
            );
        }
    }
}
