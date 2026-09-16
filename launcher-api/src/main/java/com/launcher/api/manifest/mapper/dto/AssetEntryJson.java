package com.launcher.api.manifest.mapper.dto;

import com.launcher.model.manifest.assets.AssetEntry;

public record AssetEntryJson(
        String path,
        String sha256,
        long size,
        String url
) {

    AssetEntry toAssetEntry() {
        return new AssetEntry(
                path,
                sha256,
                size,
                url
        );
    }
}
