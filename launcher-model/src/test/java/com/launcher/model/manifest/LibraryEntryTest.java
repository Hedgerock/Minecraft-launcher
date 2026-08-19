package com.launcher.model.manifest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LibraryEntryTest {

    @Test
    void should_create_library_entry() {
        //given & when
        LibraryEntry libraryEntry = new LibraryEntry("test-path");

        //then
        assertEquals("test-path", libraryEntry.path());
    }

    @Test
    void should_reject_blank_path() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LibraryEntry(" ")
        );

        assertTrue(exception.getMessage().contains("path must not be blank"));
    }

    @Test
    void should_reject_null_path() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LibraryEntry(null)
        );

        assertTrue(exception.getMessage().contains("path"));
    }

}
