package com.launcher.preparer.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PreparedVanillaAssetIndexTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.launcher.preparer.support.InvalidUriProvider#invalidUris")
    void should_reject_invalid_uri(
            String scenario,
            URI uri
    ) {
        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> new PreparedVanillaAssetIndex(
                        verifiedVanillaAssetIndex(),
                        uri
                )
        );
    }

    @Test
    void should_reject_null_uri() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new PreparedVanillaAssetIndex(
                        verifiedVanillaAssetIndex(),
                        null
                )
        );

        assertEquals(
                "uri",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_verified_vanilla_asset_index() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new PreparedVanillaAssetIndex(
                        null,
                        URI.create("https://example.org/test/")
                )
        );

        assertEquals(
                "verified",
                exception.getMessage()
        );
    }

    private VerifiedVanillaAssetIndex verifiedVanillaAssetIndex() {
        return new VerifiedVanillaAssetIndex(
                new VanillaAssetIndex(
                        List.of(
                                new VanillaAssetObject(
                                        "a".repeat(40),
                                        20
                                )
                        )
                ),
                "a".repeat(64),
                40
        );
    }
}
