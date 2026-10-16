package com.launcher.preparer.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PreparedVanillaAssetObjectTest {

    @ParameterizedTest(name = "should reject invalid resource uri: {0}")
    @MethodSource("com.launcher.preparer.support.InvalidUriProvider#invalidUris")
    void should_reject_invalid_resource_uri(
            String cause,
            URI uri
    ) {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new PreparedVanillaAssetObject(
                        assetObject(),
                        uri
                )
        );

        String expectedMessage = "Resource URI must be an absolute HTTPS URI with a " +
                "host and without user info or fragment";

        assertEquals(
                expectedMessage,
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_uri() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new PreparedVanillaAssetObject(
                        assetObject(),
                        null
                )
        );

        assertEquals("uri", exception.getMessage());
    }

    @Test
    void should_reject_null_verified_vanilla_asset_object() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new PreparedVanillaAssetObject(
                        null,
                        URI.create("https://example.org")
                )
        );

        assertEquals("verified", exception.getMessage());
    }

    private VerifiedVanillaAssetObject assetObject() {
        return new VerifiedVanillaAssetObject(
                new VanillaAssetObject(
                        "a".repeat(40),
                        20
                ),
                "a".repeat(64)
        );
    }
}
