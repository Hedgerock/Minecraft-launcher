package com.launcher.preparer.mapper;

import com.launcher.model.manifest.assets.AssetEntry;
import com.launcher.model.manifest.assets.AssetIndexMetadata;
import com.launcher.model.manifest.assets.AssetsIndex;
import com.launcher.preparer.model.PreparedVanillaAssetIndex;
import com.launcher.preparer.model.PreparedVanillaAssetObject;
import com.launcher.preparer.model.VanillaAssetIndex;
import com.launcher.preparer.model.VanillaAssetObject;
import com.launcher.preparer.model.VerifiedVanillaAssetIndex;
import com.launcher.preparer.model.VerifiedVanillaAssetObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VanillaAssetsMapperTest {

    private static final String FIRST_SHA1 = "ab" + "1".repeat(38);
    private static final String SECOND_SHA1 = "cd" + "2".repeat(38);

    private static final String FIRST_SHA256 = "3".repeat(64);
    private static final String SECOND_SHA256 = "4".repeat(64);

    private static final String FIRST_URI =
            "https://cdn.example.test/first?revision=1";

    private static final String SECOND_URI =
            "https://cdn.example.test/second?revision=2";

    private final VanillaAssetsMapper mapper = new VanillaAssetsMapper();

    @Test
    void should_map_asset_objects() {
        //given
        VanillaAssetObject first = new VanillaAssetObject(FIRST_SHA1, 11);
        VanillaAssetObject second = new VanillaAssetObject(SECOND_SHA1, 23);

        PreparedVanillaAssetObject firstPrepared = prepared(first, FIRST_SHA256, FIRST_URI);
        PreparedVanillaAssetObject secondPrepared = prepared(second, SECOND_SHA256, SECOND_URI);

        //when
        AssetsIndex result = mapper.map(
                "test-assets",
                index(first, second),
                List.of(firstPrepared, secondPrepared)
        );

        //then
        assertEquals(
                List.of(
                        new AssetEntry(
                                "assets/objects/ab/" + FIRST_SHA1,
                                FIRST_SHA256,
                                11,
                                FIRST_URI
                        ),
                        new AssetEntry(
                                "assets/objects/cd/" + SECOND_SHA1,
                                SECOND_SHA256,
                                23,
                                SECOND_URI
                        )
                ),
                result.assets()
        );
    }

    @Test
    void should_preserve_index_object_order() {
        //given
        VanillaAssetObject first = new VanillaAssetObject(FIRST_SHA1, 11);
        VanillaAssetObject second = new VanillaAssetObject(SECOND_SHA1, 23);

        PreparedVanillaAssetObject firstPrepared = prepared(first, FIRST_SHA256, FIRST_URI);
        PreparedVanillaAssetObject secondPrepared = prepared(second, SECOND_SHA256, SECOND_URI);

        //when
        AssetsIndex result = mapper.map(
                "test-assets",
                index(first, second),
                List.of(secondPrepared, firstPrepared)
        );

        //then
        assertEquals(
                List.of(
                        "assets/objects/ab/" + FIRST_SHA1,
                        "assets/objects/cd/" + SECOND_SHA1
                ),
                result.assets().stream()
                        .map(AssetEntry::path)
                        .toList()
        );
    }

    @Test
    void should_reject_missing_prepared_object() {
        //given
        VanillaAssetObject first = new VanillaAssetObject(FIRST_SHA1, 11);
        VanillaAssetObject second = new VanillaAssetObject(SECOND_SHA1, 23);

        PreparedVanillaAssetIndex index = index(first, second);
        PreparedVanillaAssetObject firstPrepared = prepared(
                first,
                FIRST_SHA256,
                FIRST_URI
        );

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> mapper.map(
                        "test-assets",
                        index,
                        List.of(firstPrepared)
                )
        );
    }

    @Test
    void should_reject_unexpected_prepared_object() {
        //given
        VanillaAssetObject first = new VanillaAssetObject(FIRST_SHA1, 11);
        VanillaAssetObject second = new VanillaAssetObject(SECOND_SHA1, 23);

        PreparedVanillaAssetIndex index = index(first);
        PreparedVanillaAssetObject firstPrepared = prepared(first, FIRST_SHA256, FIRST_URI);
        PreparedVanillaAssetObject secondPrepared = prepared(second, SECOND_SHA256, SECOND_URI);

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> mapper.map(
                        "test-assets",
                        index,
                        List.of(firstPrepared, secondPrepared)
                )
        );
    }

    @Test
    void should_reject_duplicate_prepared_object() {
        //given
        VanillaAssetObject object = new VanillaAssetObject(FIRST_SHA1, 11);
        PreparedVanillaAssetIndex index = index(object);
        PreparedVanillaAssetObject prepared = prepared(object, FIRST_SHA256, FIRST_URI);

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> mapper.map(
                        "test-assets",
                        index,
                        List.of(prepared, prepared)
                )
        );
    }

    @Test
    void should_reject_object_size_mismatch() {
        //given
        VanillaAssetObject expected = new VanillaAssetObject(FIRST_SHA1, 11);
        VanillaAssetObject conflicting = new VanillaAssetObject(FIRST_SHA1, 12);

        PreparedVanillaAssetIndex index = index(expected);

        PreparedVanillaAssetObject prepared = prepared(conflicting, FIRST_SHA256, FIRST_URI);

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> mapper.map(
                        "test-assets",
                        index,
                        List.of(prepared)
                )
        );
    }

    @Test
    void should_map_asset_index_metadata() {
        //given
        PreparedVanillaAssetIndex index = index();

        //when
        AssetsIndex result = mapper.map(
                "test-assets",
                index,
                List.of()
        );

        //then
        assertEquals(
                new AssetIndexMetadata(
                        "test-assets",
                        "assets/indexes/test-assets.json",
                        "e".repeat(64),
                        512,
                        "https://indexes.example.test/source.json?revision=7"
                ),
                result.index()
        );
    }

    @Test
    void should_map_empty_asset_index() {
        //given
        PreparedVanillaAssetIndex index = index();

        //when
        AssetsIndex result = mapper.map(
                "test-assets",
                index,
                List.of()
        );

        //then
        assertTrue(result.assets().isEmpty());
        assertEquals("test-assets", result.index().id());
    }

    @Test
    void should_reject_null_index_id() {
        //given
        PreparedVanillaAssetIndex index = index();

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> mapper.map(null, index, List.of())
        );
    }

    @Test
    void should_reject_null_index() {
        //when & then
        assertThrows(
                NullPointerException.class,
                () -> mapper.map("test-assets", null, List.of())
        );
    }

    @Test
    void should_reject_null_objects() {
        //given
        PreparedVanillaAssetIndex index = index();

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> mapper.map("test-assets", index, null)
        );
    }

    @Test
    void should_reject_null_prepared_object() {
        //given
        PreparedVanillaAssetIndex index = index();

        List<PreparedVanillaAssetObject> objects = Collections.singletonList(null);

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> mapper.map("test-assets", index, objects)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "../outside", "CON"})
    void should_reject_invalid_index_id(String indexId) {
        //given
        PreparedVanillaAssetIndex index = index();

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> mapper.map(indexId, index, List.of())
        );
    }

    private PreparedVanillaAssetIndex index(VanillaAssetObject... objects) {
        return new PreparedVanillaAssetIndex(
                new VerifiedVanillaAssetIndex(
                        new VanillaAssetIndex(
                                List.of(objects)
                        ),
                        "e".repeat(64),
                        512
                ),
                URI.create(
                        "https://indexes.example.test/source.json?revision=7"
                )
        );
    }

    private PreparedVanillaAssetObject prepared(
            VanillaAssetObject object,
            String sha256,
            String uri
    ) {
        return new PreparedVanillaAssetObject(
                new VerifiedVanillaAssetObject(
                        object,
                        sha256
                ),
                URI.create(uri)
        );
    }
}
