package com.launcher.preparer.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VanillaAssetIndexTest {

    @Test
    void should_reject_asset_objects_mutation_from_accessor() {
        //given
        List<VanillaAssetObject> assetObjects = new ArrayList<>(
                List.of(
                        vanillaAssetObject("a", 10),
                        vanillaAssetObject("b", 10)
                )
        );

        VanillaAssetIndex index = new VanillaAssetIndex(assetObjects);

        //when & then
        assertThrows(
                UnsupportedOperationException.class,
                () -> index.objects().add(
                        vanillaAssetObject("c", 10)
                )
        );
    }

    @Test
    void should_create_immutable_vanilla_asset_objects() {
        //given
        List<VanillaAssetObject> assetObjects = new ArrayList<>(
                List.of(
                        vanillaAssetObject("a", 12),
                        vanillaAssetObject("b", 12)
                )
        );

        VanillaAssetIndex index = new VanillaAssetIndex(assetObjects);

        //when
        assetObjects.add(vanillaAssetObject("c", 12));

        //then
        assertEquals(
                List.of(
                        vanillaAssetObject("a", 12),
                        vanillaAssetObject("b", 12)
                ),
                index.objects()
        );

    }

    private VanillaAssetObject vanillaAssetObject(
            String name,
            int size
    ) {
        return new VanillaAssetObject(
                name.repeat(40),
                size
        );
    }
}
