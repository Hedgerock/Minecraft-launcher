package com.launcher.model.manifest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceEntryTest {
    private static final String DEFAULT_PATH = "test-path";
    private static final String DEFAULT_URL = "https://example.com";
    private static final String DEFAULT_SHA256 = "sha256";
    private static final long DEFAULT_SIZE = 123L;

    @Test
    void should_create_valid_resource_entry() {
        //given & when
        ResourceEntry resourceEntry = getValidResourceEntry();

        //then
        assertEquals(DEFAULT_PATH, resourceEntry.path());
        assertEquals(DEFAULT_SHA256, resourceEntry.sha256());
        assertEquals(DEFAULT_SIZE, resourceEntry.size());
        assertEquals(DEFAULT_URL, resourceEntry.url());
    }

    @Test
    void should_reject_blank_url() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                this::getResourceEntryWithBlankUrl
        );

        assertTrue(exception.getMessage().contains("url must not be blank"));
    }

    @Test
    void should_reject_blank_sha256() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                this::getResourceEntryWithBlankSha256
        );

        assertTrue(exception.getMessage().contains("sha256 must not be blank"));
    }

    @Test
    void should_reject_blank_path() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                this::getResourceEntryWithBlankPath
        );

        assertTrue(exception.getMessage().contains("path must not be blank"));
    }

    @Test
    void should_reject_negative_size() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                this::getResourceEntryWithNegativeSize
        );

        assertTrue(exception.getMessage().contains("size must be positive"));
    }

    @Test
    void should_reject_null_url() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                this::getResourceEntryWithNullUrl
        );

        assertTrue(exception.getMessage().contains("url"));
    }

    @Test
    void should_reject_null_sha256() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                this::getResourceEntryWithNullSha256
        );

        assertTrue(exception.getMessage().contains("sha256"));
    }

    @Test
    void should_reject_null_path() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                this::getResourceEntryWithNullPath
        );

        assertTrue(exception.getMessage().contains("path"));
    }

    private ResourceEntry getResourceEntry(
            String path,
            String sha256,
            long size,
            String url
    ) {
        return new ResourceEntry(path, sha256, size, url);
    }

    private ResourceEntry getValidResourceEntry() {
        return getResourceEntry(DEFAULT_PATH, DEFAULT_SHA256, DEFAULT_SIZE, DEFAULT_URL);
    }

    private void getResourceEntryWithBlankPath() {
        getResourceEntry(" ", DEFAULT_SHA256, DEFAULT_SIZE, DEFAULT_URL);
    }

    private void getResourceEntryWithNullPath() {
        getResourceEntry(null, DEFAULT_SHA256, DEFAULT_SIZE, DEFAULT_URL);
    }

    private void getResourceEntryWithBlankSha256() {
        getResourceEntry(DEFAULT_PATH, " ", DEFAULT_SIZE, DEFAULT_URL);
    }

    private void getResourceEntryWithNullSha256() {
        getResourceEntry(DEFAULT_PATH, null, DEFAULT_SIZE, DEFAULT_URL);
    }

    private void getResourceEntryWithNegativeSize() {
        getResourceEntry(DEFAULT_PATH, DEFAULT_SHA256, -1L, DEFAULT_URL);
    }

    private void getResourceEntryWithNullUrl() {
        getResourceEntry(DEFAULT_PATH, DEFAULT_SHA256, DEFAULT_SIZE, null);
    }

    private void getResourceEntryWithBlankUrl() {
        getResourceEntry(DEFAULT_PATH, DEFAULT_SHA256, DEFAULT_SIZE, " ");
    }
}
