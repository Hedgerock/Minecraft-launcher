package com.launcher.model.manifest.assets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssetIndexMetadataTest {

    @Test
    void should_return_asset_index_metadata() {
        //given & when
        AssetIndexMetadata metadata = getMetadata(
                "current-id",
                "assets/indexes/current-id.json",
                "sha-256",
                123L,
                "https://example.com"
        );

        //then
        assertEquals(
                "current-id",
                metadata.id()
        );
        assertEquals(
                "assets/indexes/current-id.json",
                metadata.path()
        );
        assertEquals(
                "sha-256",
                metadata.sha256()
        );
        assertEquals(
                123L,
                metadata.size()
        );
        assertEquals(
                "https://example.com",
                metadata.url()
        );
    }

    @Test
    void should_reject_negative_size() {
        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> getMetadataWithSize(-1)
        );
    }

    @ParameterizedTest(name = "should reject blank {0}")
    @MethodSource("providedBlankFields")
    void should_reject_blank_fields(
            String field,
            Executable executable
    ) {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                executable
        );

        String expectedMessage = field + " must not be blank";

        assertEquals(
                expectedMessage,
                exception.getMessage()
        );
    }

    @ParameterizedTest(name = "should reject null {0}")
    @MethodSource("providedNullFields")
    void should_reject_null_fields(
            String field,
            Executable executable
    ) {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                executable
        );

        assertEquals(field, exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "assets/indexes/other.json",
            "assets/indexes/./1.21.json",
            "assets/indexes/../1.21.json",
            "assets\\indexes\\1.21.json",
            "assets/indexes/1.21.json/"
    })
    void should_reject_assets_index_path_not_matching_id(
            String path
    ) {
        //given
        String id = "1.21";

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> createMetadata(id, path)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "bad:id",
            "bad*id",
            "bad?id",
            "bad\"id",
            "bad<id",
            "bad>id",
            "bad|id",
            "bad/id",
            "bad\\id",
            ".",
            "..",
            "CON",
            "con",
            "PRN",
            "AUX",
            "NUL",
            "COM1",
            "LPT1",
            "CON.txt",
            "NUL.json"
    })
    void should_reject_unsafe_assets_index_id(String id) {
        //given
        String path = "assets/indexes/" + id + ".json";

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> createMetadata(id, path)
        );
    }

    private void createMetadata(
            String id,
            String path
    ) {
        new AssetIndexMetadata(
                id,
                path,
                "sha-256",
                123L,
                "https://test-example.org"
        );
    }

    private static Stream<Arguments> providedNullFields() {
        return Stream.of(
                Arguments.of(
                        "id",
                        (Executable) () -> getMetadataWithId(null)
                ),
                Arguments.of(
                        "path",
                        (Executable) () -> getMetadataWithPath(null)
                ),
                Arguments.of(
                        "sha256",
                        (Executable) () -> getMetadataWithSha256(null)
                ),
                Arguments.of(
                        "url",
                        (Executable) () -> getMetadataWithUrl(null)
                )
        );
    }

    private static Stream<Arguments> providedBlankFields() {
        return Stream.of(
                Arguments.of(
                        "id",
                        (Executable) () -> getMetadataWithId(" ")
                ),
                Arguments.of(
                        "path",
                        (Executable) () -> getMetadataWithPath(" ")
                ),
                Arguments.of(
                        "sha256",
                        (Executable) () -> getMetadataWithSha256(" ")
                ),
                Arguments.of(
                        "url",
                        (Executable) () -> getMetadataWithUrl(" ")
                )
        );
    }

    private static AssetIndexMetadata getMetadata(
            String id,
            String path,
            String sha256,
            long size,
            String url
    ) {
        return new AssetIndexMetadata(
                id,
                path,
                sha256,
                size,
                url
        );
    }

    private static void getMetadataWithId(String id) {
        getMetadata(
                id,
                "assets/indexes/" + id + ".json",
                "sha-256",
                123L,
                "https://example.org"
        );
    }

    private static void getMetadataWithPath(String path) {
        getMetadata(
                "current-id",
                path,
                "sha-256",
                123L,
                "https://example.org"
        );
    }

    private static void getMetadataWithSha256(String sha256) {
        getMetadata(
                "current-id",
                "assets/indexes/current-id.json",
                sha256,
                321L,
                "https://another-example.org"
        );
    }

    private void getMetadataWithSize(long size) {
        getMetadata(
                "current-id",
                "assets/indexes/current-id.json",
                "sha-256",
                size,
                "https://another-example.org"
        );
    }

    private static void getMetadataWithUrl(String url) {
        getMetadata(
                "current-id",
                "assets/indexes/current-id.json",
                "sha-256",
                321L,
                url
        );
    }
}
