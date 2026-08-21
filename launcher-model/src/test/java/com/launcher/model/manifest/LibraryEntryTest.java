package com.launcher.model.manifest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LibraryEntryTest {
    private static final String DEFAULT_PATH = "test-path";
    private static final String DEFAULT_SHA256 = "sha256";
    private static final long DEFAULT_SIZE = 123L;
    private static final String DEFAULT_URL = "https://example.com";

    @Test
    void should_reject_blank_url() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                this::getLibraryEntryWithBlankUrl
        );

        assertTrue(exception.getMessage().contains("url must not be blank"));
    }

    @Test
    void should_reject_null_url() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                this::getLibraryEntryWithNullUrl
        );

        assertTrue(exception.getMessage().contains("url"));
    }

    @Test
    void should_reject_negative_size() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                this::getLibraryEntryWithNegativeSize
        );

        assertTrue(exception.getMessage().contains("size must be positive"));
    }

    @Test
    void should_reject_blank_sha256() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                this::getLibraryEntryWithBlankSha256
        );

        assertTrue(exception.getMessage().contains("sha256 must not be blank"));
    }

    @Test
    void should_reject_null_sha256() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                this::getLibraryEntryWithNullSha256
        );

        assertTrue(exception.getMessage().contains("sha256"));
    }

    @Test
    void should_create_library_entry() {
        //given & when
        LibraryEntry libraryEntry = getValidLibraryEntry();

        //then
        assertEquals("test-path", libraryEntry.path());
        assertEquals("sha256", libraryEntry.sha256());
        assertEquals(123L, libraryEntry.size());
        assertEquals("https://example.com", libraryEntry.url());
    }

    @Test
    void should_reject_blank_path() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                this::getLibraryEntryWithBlankPath
        );

        assertTrue(exception.getMessage().contains("path must not be blank"));
    }

    @Test
    void should_reject_null_path() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                this::getLibraryEntryWithNullPath
        );

        assertTrue(exception.getMessage().contains("path"));
    }


    private LibraryEntry libraryEntry(
            String path,
            String sha256,
            long size,
            String url
    ) {
        return new LibraryEntry(path, sha256, size, url);
    }

    private LibraryEntry getValidLibraryEntry() {
        return libraryEntry(DEFAULT_PATH, DEFAULT_SHA256, DEFAULT_SIZE, DEFAULT_URL);
    }

    private void getLibraryEntryWithBlankPath() {
        libraryEntry(" ", DEFAULT_SHA256, DEFAULT_SIZE, DEFAULT_URL);
    }

    private void getLibraryEntryWithNullPath() {
        libraryEntry(null, DEFAULT_SHA256, DEFAULT_SIZE, DEFAULT_URL);
    }

    private void getLibraryEntryWithBlankSha256() {
        libraryEntry(DEFAULT_PATH, " ", DEFAULT_SIZE, DEFAULT_URL);
    }

    private void getLibraryEntryWithNullSha256() {
        libraryEntry(DEFAULT_PATH, null, DEFAULT_SIZE, DEFAULT_URL);
    }

    private void getLibraryEntryWithNegativeSize() {
        libraryEntry(DEFAULT_PATH, DEFAULT_SHA256, -1L, DEFAULT_URL);
    }

    private void getLibraryEntryWithBlankUrl() {
        libraryEntry(DEFAULT_PATH, DEFAULT_SHA256, DEFAULT_SIZE, " ");
    }

    private void getLibraryEntryWithNullUrl() {
        libraryEntry(DEFAULT_PATH, DEFAULT_SHA256, DEFAULT_SIZE, null);
    }

}
