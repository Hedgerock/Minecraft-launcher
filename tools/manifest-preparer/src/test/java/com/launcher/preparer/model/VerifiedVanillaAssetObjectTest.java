package com.launcher.preparer.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class VerifiedVanillaAssetObjectTest {

    @Test
    void should_reject_sha256_with_invalid_length() {
        //given
        String sha256 = "a".repeat(63);

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> new VerifiedVanillaAssetObject(getObject(), sha256)
        );
    }

    @Test
    void should_reject_sha256_with_non_hex_characters() {
        //given
        String sha256 = "z".repeat(64);

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> new VerifiedVanillaAssetObject(getObject(), sha256)
        );
    }

    private VanillaAssetObject getObject() {
        return new VanillaAssetObject("a".repeat(40), 20);
    }
}
