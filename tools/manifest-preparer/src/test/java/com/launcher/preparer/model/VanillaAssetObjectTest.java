package com.launcher.preparer.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VanillaAssetObjectTest {

    @Test
    void should_reject_negative_size() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new VanillaAssetObject("a".repeat(40), -1)
        );

        assertEquals(
                "size must not be negative",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_sha1_with_invalid_length() {
        //given
        String sha1 = "a".repeat(39);

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> new VanillaAssetObject(sha1, 20)
        );
    }

    @Test
    void should_reject_sha1_with_non_hex_characters() {
        //given
        String sha1 = "z".repeat(40);

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> new VanillaAssetObject(sha1, 20)
        );
    }
}
