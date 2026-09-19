package com.launcher.model.manifest.assets;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssetsIndexTest {

    @Test
    void should_access_empty_assets() {
        //given & when
        AssetsIndex result = new AssetsIndex(List.of());

        //then
        assertTrue(result.assets().isEmpty());
    }

    @Test
    void should_create_asset_index() {
        //given
        AssetEntry first = getAssetEntry("test");
        AssetEntry second = getAssetEntry("test-second");
        List<AssetEntry> assets = new ArrayList<>(List.of(first, second));

        //when
        AssetsIndex result = new AssetsIndex(assets);

        //then
        assertEquals(
                List.of(first, second),
                result.assets()
        );
    }

    @Test
    void should_reject_assets_mutation_from_accessor() {
        //given
        AssetEntry first = getAssetEntry("test");
        AssetEntry second = getAssetEntry("test-second");
        List<AssetEntry> assets = new ArrayList<>(List.of(first));

        AssetsIndex index = new AssetsIndex(assets);

        //when
        assertThrows(
                UnsupportedOperationException.class,
                () -> index.assets().add(second)
        );
    }

    @Test
    void should_create_immutable_assets() {
        //given
        AssetEntry first = getAssetEntry("test");
        AssetEntry second = getAssetEntry("test-second");
        List<AssetEntry> assets = new ArrayList<>(List.of(first));

        AssetsIndex index = new AssetsIndex(assets);

        //when
        assets.add(second);

        //then
        assertEquals(
                List.of(first),
                index.assets()
        );
    }

    @Test
    void should_reject_null_asset() {
        //given
        List<AssetEntry> assets = new ArrayList<>();
        assets.add(null);

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new AssetsIndex(assets)
        );

        assertEquals("asset", exception.getMessage());
    }

    @Test
    void should_reject_null_assets() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new AssetsIndex(null)
        );

        assertEquals("assets", exception.getMessage());
    }

    private AssetEntry getAssetEntry(String assetName) {
        return new AssetEntry(
                assetName + ".jar",
                assetName + "-sha256",
                123L,
                "https://test-url.com/" + assetName + ".jar"
        );
    }
}
