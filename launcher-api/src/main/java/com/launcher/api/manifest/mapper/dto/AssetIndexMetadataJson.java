package com.launcher.api.manifest.mapper.dto;

import com.launcher.model.manifest.assets.AssetIndexMetadata;

public record AssetIndexMetadataJson(
        String id,
        String path,
        String sha256,
        long size,
        String url
) {

    AssetIndexMetadata toAssetIndexMetadata() {
        return new AssetIndexMetadata(id, path, sha256, size, url);
    }
}
