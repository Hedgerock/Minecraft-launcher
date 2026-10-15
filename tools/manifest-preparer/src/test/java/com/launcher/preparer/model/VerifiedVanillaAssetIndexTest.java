package com.launcher.preparer.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VerifiedVanillaAssetIndexTest {

    @ParameterizedTest(name = "should_reject_null_field {0}")
    @MethodSource("nullFields")
    void should_reject_null_field(String field, Executable executable) {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                executable
        );

        assertEquals(field, exception.getMessage());
    }

    @Test
    void should_reject_negative_size() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new VerifiedVanillaAssetIndex(
                        assetIndex(),
                        "a".repeat(64),
                        -1
                )
        );

        assertEquals(
                "size must not be negative",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_sha256_with_invalid_length() {
        //given
        String sha256 = "a".repeat(63);

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> new VerifiedVanillaAssetIndex(
                        assetIndex(),
                        sha256,
                        20
                )
        );
    }

    @Test
    void should_reject_sha256_with_non_hex_characters() {
        //given
        String sha256 = "z".repeat(64);

        //when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> new VerifiedVanillaAssetIndex(
                        assetIndex(),
                        sha256,
                        20
                )
        );
    }

    private static Stream<Arguments> nullFields() {
        return Stream.of(
                Arguments.of(
                        "index",
                        (Executable) () -> new VerifiedVanillaAssetIndex(null, "a".repeat(64), 20)
                ),
                Arguments.of(
                        "sha256",
                        (Executable) () -> new VerifiedVanillaAssetIndex(assetIndex(), null, 20)
                )
        );
    }

    private static VanillaAssetIndex assetIndex() {
        return new VanillaAssetIndex(
                List.of(
                        new VanillaAssetObject(
                                "a".repeat(40),
                                20
                        )
                )
        );
    }
}
