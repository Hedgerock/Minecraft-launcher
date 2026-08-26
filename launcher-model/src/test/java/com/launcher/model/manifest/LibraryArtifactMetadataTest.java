package com.launcher.model.manifest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LibraryArtifactMetadataTest {
    private static final String DEFAULT_PATH = "test-path";
    private static final String DEFAULT_SHA256 = "sha256";
    private static final long DEFAULT_SIZE = 123L;
    private static final String DEFAULT_URL = "https://example.com";

    @Test
    void should_reject_null_url() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                this::getLibraryArtifactMetadataWithNullUrl
        );

        assertTrue(exception.getMessage().contains("url"));
    }

    @Test
    void should_reject_blank_url() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                this::getLibraryArtifactMetadataWithBlankUrl
        );

        assertTrue(exception.getMessage().contains("url"));
    }

    @Test
    void should_reject_null_sha256() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                this::getLibraryArtifactMetadataWithNullSha256
        );

        assertTrue(exception.getMessage().contains("sha256"));
    }

    @Test
    void should_reject_blank_sha256() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                this::getLibraryArtifactMetadataWithBlankSha256
        );

        assertTrue(exception.getMessage().contains("sha256 must not be blank"));
    }

    @Test
    void should_create_valid_library_artifact_metadata() {
        //given & when
        LibraryArtifactMetadata libraryArtifactMetadata = getValidLibraryArtifactMetadata();

        //then
        assertEquals(DEFAULT_PATH, libraryArtifactMetadata.path());
        assertEquals(DEFAULT_SHA256, libraryArtifactMetadata.sha256());
        assertEquals(DEFAULT_SIZE, libraryArtifactMetadata.size());
        assertEquals(DEFAULT_URL, libraryArtifactMetadata.url());
    }

    @Test
    void should_reject_null_path() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                this::getLibraryArtifactMetadataWithNullPath
        );

        assertTrue(exception.getMessage().contains("path"));
    }

    @Test
    void should_reject_blank_path() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                this::getLibraryArtifactMetadataWithBlankPath
        );

        assertTrue(exception.getMessage().contains("path must not be blank"));
    }

    @Test
    void should_reject_negative_size() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                this::getLibraryArtifactMetadataWithNegativeSize
        );

        assertTrue(exception.getMessage().contains("size must be positive"));
    }

    private LibraryArtifactMetadata createLibraryArtifactMetadata(
            String path,
            String sha256,
            long size,
            String url
    ) {
        return new LibraryArtifactMetadata(path, sha256, size, url);
    }

    private LibraryArtifactMetadata getValidLibraryArtifactMetadata() {
        return createLibraryArtifactMetadata(DEFAULT_PATH, DEFAULT_SHA256, DEFAULT_SIZE, DEFAULT_URL);
    }

    private void getLibraryArtifactMetadataWithBlankPath() {
        createLibraryArtifactMetadata(" ", DEFAULT_SHA256, DEFAULT_SIZE, DEFAULT_URL);
    }

    private void getLibraryArtifactMetadataWithNullPath() {
        createLibraryArtifactMetadata(null, DEFAULT_SHA256, DEFAULT_SIZE, DEFAULT_URL);
    }

    private void getLibraryArtifactMetadataWithBlankSha256() {
        createLibraryArtifactMetadata(DEFAULT_PATH, " ", DEFAULT_SIZE, DEFAULT_URL);
    }

    private void getLibraryArtifactMetadataWithNullSha256() {
        createLibraryArtifactMetadata(DEFAULT_PATH, null, DEFAULT_SIZE, DEFAULT_URL);
    }

    private void getLibraryArtifactMetadataWithNegativeSize() {
        createLibraryArtifactMetadata(DEFAULT_PATH, DEFAULT_SHA256, -1L, DEFAULT_URL);
    }

    private void getLibraryArtifactMetadataWithBlankUrl() {
        createLibraryArtifactMetadata(DEFAULT_PATH, DEFAULT_SHA256, DEFAULT_SIZE, " ");
    }

    private void getLibraryArtifactMetadataWithNullUrl() {
        createLibraryArtifactMetadata(DEFAULT_PATH, DEFAULT_SHA256, DEFAULT_SIZE, null);
    }
}
