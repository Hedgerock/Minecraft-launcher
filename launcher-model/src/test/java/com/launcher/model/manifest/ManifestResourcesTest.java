package com.launcher.model.manifest;

import com.launcher.model.support.ManifestResourcesFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ManifestResourcesTest {
    private ManifestResourcesFixture manifestResourcesFixture;

    @BeforeEach
    void setUp() {
        manifestResourcesFixture = new ManifestResourcesFixture();
    }

    @Test
    void should_return_empty_resource_list_when_manifest_has_no_files_and_libraries() {
        //given & when
        Manifest manifestWithEmptyFiles = manifestResourcesFixture.getManifestWithoutResources();
        List<ResourceEntry> resourceEntries = ManifestResources.from(manifestWithEmptyFiles);

        //then
        assertTrue(resourceEntries.isEmpty());
    }

    @Test
    void should_reject_null_manifest() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> ManifestResources.from(null)
        );

        assertTrue(exception.getMessage().contains("manifest"));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void should_return_immutable_resource_list() {
        //given
        Manifest manifest = manifestResourcesFixture.getManifest();
        List<ResourceEntry> resourceEntries = ManifestResources.from(manifest);
        ResourceEntry candidate = manifestResourcesFixture.generateResourceEntry(
                "new-path",
                "new-sha256",
                12345L,
                "new-url"
        );

        //when & then
        assertThrows(
                UnsupportedOperationException.class,
                () -> resourceEntries.add(candidate)
        );
    }

    @Test
    void should_preserve_resource_order() {
        //given & when
        Manifest manifest = manifestResourcesFixture.getManifest();
        List<ResourceEntry> resourceEntries = ManifestResources.from(manifest);

        ResourceEntry resourceEntry = manifestResourcesFixture.getFileEntry();
        ResourceEntry libraryEntry = manifestResourcesFixture.getLibraryEntry();

        //then
        assertEquals(resourceEntry, resourceEntries.get(0));
        assertEquals(libraryEntry, resourceEntries.get(1));
    }

    @Test
    void should_collect_file_and_library_resources_from_manifest() {
        //given & when
        Manifest manifest = manifestResourcesFixture.getManifest();
        List<ResourceEntry> resourceEntries = ManifestResources.from(manifest);

        ResourceEntry resourceEntry = manifestResourcesFixture.getFileEntry();
        ResourceEntry libraryEntry = manifestResourcesFixture.getLibraryEntry();

        //then
        assertEquals(
                List.of(resourceEntry, libraryEntry),
                resourceEntries
        );
    }

}
