package com.launcher.preparer.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestDigestsTest {

    @ParameterizedTest
    @CsvSource({
            "'', da39a3ee5e6b4b0d3255bfef95601890afd80709",
            "abc, a9993e364706816aba3e25717850c26c9cd0d89d"
    })
    void should_return_sha1_for_known_content(
            String content,
            String expectedDigest
    ) {
        //given
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        //when
        String result = TestDigests.sha1(bytes);

        //then
        assertEquals(expectedDigest, result);
    }

    @ParameterizedTest
    @CsvSource({
            "'', e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            "abc, ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
    })
    void should_return_sha256_for_known_content(
            String content,
            String expectedDigest
    ) {
        //given
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        //when
        String result = TestDigests.sha256(bytes);

        //then
        assertEquals(expectedDigest, result);
    }

    @Test
    void should_reject_null_bytes_for_sha1() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> TestDigests.sha1(null)
        );

        assertEquals("bytes", exception.getMessage());
    }

    @Test
    void should_reject_null_bytes_for_sha256() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> TestDigests.sha256(null)
        );

        assertEquals("bytes", exception.getMessage());
    }
}
