package com.launcher.model.manifest.assets;

import java.util.List;
import java.util.Objects;

public record AssetsIndex(
        List<AssetEntry> assets
) {

    public AssetsIndex {
        Objects.requireNonNull(assets, "assets");

        assets.forEach(
                asset -> Objects.requireNonNull(asset, "asset")
        );

        assets = List.copyOf(assets);
    }
}
