package com.launcher.model.manifest.assets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssetEntryTest {
    private static final String DEFAULT_PATH = "default_path.jar";
    private static final String DEFAULT_SHA256 = "default_sha256";
    private static final long DEFAULT_SIZE = 123L;
    private static final String DEFAULT_URL = "https://test-url.com/default_url.jar";

    @Test
    void should_create_asset_entry() {
        //given & when
        AssetEntry result = new AssetEntry(DEFAULT_PATH, DEFAULT_SHA256, DEFAULT_SIZE, DEFAULT_URL);

        //then
        assertEquals(DEFAULT_PATH, result.path());
        assertEquals(DEFAULT_SHA256, result.sha256());
        assertEquals(DEFAULT_SIZE, result.size());
        assertEquals(DEFAULT_URL, result.url());
    }

    @Test
    void should_reject_negative_size() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new AssetEntry(DEFAULT_PATH, DEFAULT_SHA256, -1, DEFAULT_URL)
        );

        assertEquals("size must be positive", exception.getMessage());
    }

    @Test
    void should_reject_blank_url() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new AssetEntry(DEFAULT_PATH, DEFAULT_SHA256, DEFAULT_SIZE, " ")
        );

        assertEquals("url must not be blank", exception.getMessage());
    }

    @Test
    void should_reject_null_url() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new AssetEntry(DEFAULT_PATH, DEFAULT_SHA256, DEFAULT_SIZE, null)
        );

        assertEquals("url", exception.getMessage());
    }

    @Test
    void should_reject_blank_sha256() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new AssetEntry(DEFAULT_PATH, " ", DEFAULT_SIZE, DEFAULT_URL)
        );

        assertEquals("sha256 must not be blank", exception.getMessage());
    }

    @Test
    void should_reject_null_sha256() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new AssetEntry(DEFAULT_PATH, null, DEFAULT_SIZE, DEFAULT_URL)
        );

        assertEquals("sha256", exception.getMessage());
    }

    @Test
    void should_reject_blank_path() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new AssetEntry(" ", DEFAULT_SHA256, DEFAULT_SIZE, DEFAULT_URL)
        );

        assertEquals("path must not be blank", exception.getMessage());
    }

    @Test
    void should_reject_null_path() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new AssetEntry(null, DEFAULT_SHA256, DEFAULT_SIZE, DEFAULT_URL)
        );

        assertEquals("path", exception.getMessage());
    }
}
