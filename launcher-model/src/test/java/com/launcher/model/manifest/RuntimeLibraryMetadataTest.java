package com.launcher.model.manifest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RuntimeLibraryMetadataTest {
    private static final String DEFAULT_PATH = "test-path";
    private static final String DEFAULT_SHA256 = "sha256";
    private static final long DEFAULT_SIZE = 123L;
    private static final String DEFAULT_URL = "https://example.com";

    @Test
    void should_reject_null_url() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                this::getRuntimeLibraryMetadataWithNullUrl
        );

        assertTrue(exception.getMessage().contains("url"));
    }

    @Test
    void should_reject_blank_url() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                this::getRuntimeLibraryMetadataWithBlankUrl
        );

        assertTrue(exception.getMessage().contains("url"));
    }

    @Test
    void should_reject_null_sha256() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                this::getRuntimeLibraryMetadataWithNullSha256
        );

        assertTrue(exception.getMessage().contains("sha256"));
    }

    @Test
    void should_reject_blank_sha256() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                this::getRuntimeLibraryMetadataWithBlankSha256
        );

        assertTrue(exception.getMessage().contains("sha256 must not be blank"));
    }

    @Test
    void should_create_valid_runtime_library_metadata() {
        //given & when
        RuntimeLibraryMetadata runtimeLibraryMetadata = getValidRuntimeLibraryMetadata();

        //then
        assertEquals(DEFAULT_PATH, runtimeLibraryMetadata.path());
        assertEquals(DEFAULT_SHA256, runtimeLibraryMetadata.sha256());
        assertEquals(DEFAULT_SIZE, runtimeLibraryMetadata.size());
        assertEquals(DEFAULT_URL, runtimeLibraryMetadata.url());
    }

    @Test
    void should_reject_null_path() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                this::getRuntimeLibraryMetadataWithNullPath
        );

        assertTrue(exception.getMessage().contains("path"));
    }

    @Test
    void should_reject_blank_path() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                this::getRuntimeLibraryMetadataWithBlankPath
        );

        assertTrue(exception.getMessage().contains("path must not be blank"));
    }

    @Test
    void should_reject_negative_size() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                this::getRuntimeLibraryMetadataWithNegativeSize
        );

        assertTrue(exception.getMessage().contains("size must be positive"));
    }

    private RuntimeLibraryMetadata createRuntimeLibraryMetadata(
            String path,
            String sha256,
            long size,
            String url
    ) {
        return new RuntimeLibraryMetadata(path, sha256, size, url);
    }

    private RuntimeLibraryMetadata getValidRuntimeLibraryMetadata() {
        return createRuntimeLibraryMetadata(DEFAULT_PATH, DEFAULT_SHA256, DEFAULT_SIZE, DEFAULT_URL);
    }

    private void getRuntimeLibraryMetadataWithBlankPath() {
        createRuntimeLibraryMetadata(" ", DEFAULT_SHA256, DEFAULT_SIZE, DEFAULT_URL);
    }

    private void getRuntimeLibraryMetadataWithNullPath() {
        createRuntimeLibraryMetadata(null, DEFAULT_SHA256, DEFAULT_SIZE, DEFAULT_URL);
    }

    private void getRuntimeLibraryMetadataWithBlankSha256() {
        createRuntimeLibraryMetadata(DEFAULT_PATH, " ", DEFAULT_SIZE, DEFAULT_URL);
    }

    private void getRuntimeLibraryMetadataWithNullSha256() {
        createRuntimeLibraryMetadata(DEFAULT_PATH, null, DEFAULT_SIZE, DEFAULT_URL);
    }

    private void getRuntimeLibraryMetadataWithNegativeSize() {
        createRuntimeLibraryMetadata(DEFAULT_PATH, DEFAULT_SHA256, -1L, DEFAULT_URL);
    }

    private void getRuntimeLibraryMetadataWithBlankUrl() {
        createRuntimeLibraryMetadata(DEFAULT_PATH, DEFAULT_SHA256, DEFAULT_SIZE, " ");
    }

    private void getRuntimeLibraryMetadataWithNullUrl() {
        createRuntimeLibraryMetadata(DEFAULT_PATH, DEFAULT_SHA256, DEFAULT_SIZE, null);
    }

}
