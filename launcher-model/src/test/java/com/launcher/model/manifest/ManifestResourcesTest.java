package com.launcher.model.manifest;

import com.launcher.model.manifest.assets.AssetIndexMetadata;
import com.launcher.model.manifest.assets.AssetsIndex;
import com.launcher.model.support.ManifestResourcesFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManifestResourcesTest {
    private ManifestResourcesFixture manifestResourcesFixture;

    @BeforeEach
    void setUp() {
        manifestResourcesFixture = new ManifestResourcesFixture();
    }

    @Test
    void should_return_empty_resource_list_when_manifest_has_no_files_libraries_and_assets() {
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
        ResourceEntry assetEntry = manifestResourcesFixture.getAssetEntry();

        //then
        assertEquals(resourceEntry, resourceEntries.get(0));
        assertEquals(libraryEntry, resourceEntries.get(1));
        assertEquals(assetEntry, resourceEntries.get(2));
    }

    @Test
    void should_collect_manifest_resources_with_asset_index_metadata() {
        //given & when
        Manifest manifest = getManifestWithAssetIndexMetadata();
        List<ResourceEntry> resourceEntries = ManifestResources.from(manifest);

        ResourceEntry resourceEntry = manifestResourcesFixture.getFileEntry();
        ResourceEntry libraryEntry = manifestResourcesFixture.getLibraryEntry();
        ResourceEntry assetEntry = manifestResourcesFixture.getAssetEntry();

        //then
        AssetIndexMetadata metadata = manifest.assetsIndex().index();
        ResourceEntry metadataResourceEntry = manifestResourcesFixture.fromAssetIndexMetadata(metadata);

        assertEquals(
                List.of(
                        resourceEntry,
                        libraryEntry,
                        assetEntry,
                        metadataResourceEntry
                ),
                resourceEntries
        );
    }

    @Test
    void should_collect_manifest_resources() {
        //given & when
        Manifest manifest = manifestResourcesFixture.getManifest();
        List<ResourceEntry> resourceEntries = ManifestResources.from(manifest);

        ResourceEntry resourceEntry = manifestResourcesFixture.getFileEntry();
        ResourceEntry libraryEntry = manifestResourcesFixture.getLibraryEntry();
        ResourceEntry assetEntry = manifestResourcesFixture.getAssetEntry();

        //then
        assertEquals(
                List.of(resourceEntry, libraryEntry, assetEntry),
                resourceEntries
        );
    }

    private Manifest getManifestWithAssetIndexMetadata() {
        Manifest original = manifestResourcesFixture.getManifest();

        AssetsIndex assetsIndex = new AssetsIndex(
                original.assetsIndex().assets(),
                new AssetIndexMetadata(
                        "current-id",
                        "assets/indexes/current-id.json",
                        "sha-256",
                        123L,
                        "https://example.org"
                )
        );

        return new Manifest(
                original.minecraftVersion(),
                original.loader(),
                original.files(),
                original.launchInfo(),
                original.libraries(),
                assetsIndex
        );
    }
}
