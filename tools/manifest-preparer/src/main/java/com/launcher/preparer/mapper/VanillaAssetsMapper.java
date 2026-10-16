package com.launcher.preparer.mapper;

import com.launcher.model.manifest.assets.AssetEntry;
import com.launcher.model.manifest.assets.AssetIndexMetadata;
import com.launcher.model.manifest.assets.AssetsIndex;
import com.launcher.preparer.model.PreparedVanillaAssetIndex;
import com.launcher.preparer.model.PreparedVanillaAssetObject;
import com.launcher.preparer.model.VanillaAssetObject;
import com.launcher.preparer.model.VerifiedVanillaAssetIndex;
import com.launcher.preparer.model.VerifiedVanillaAssetObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class VanillaAssetsMapper {

    public AssetsIndex map(
            String indexId,
            PreparedVanillaAssetIndex index,
            List<PreparedVanillaAssetObject> objects
    ) {
        Objects.requireNonNull(indexId, "indexId");
        Objects.requireNonNull(index, "index");
        Objects.requireNonNull(objects, "objects");

        AssetIndexMetadata indexMetadata = mapIndexMetadata(indexId, index);

        Map<String, PreparedVanillaAssetObject> objectsBySha1 = indexBySha1(objects);
        List<AssetEntry> assets = new ArrayList<>();

        for (VanillaAssetObject expected : index.verified().index().objects()) {
            PreparedVanillaAssetObject prepared = objectsBySha1.remove(expected.sha1());

            if (prepared == null) {
                throw new IllegalArgumentException(
                        "Missing prepared asset object: " + expected.sha1()
                );
            }

            if (prepared.verified().object().size() != expected.size()) {
                throw new IllegalArgumentException(
                        "Prepared asset object size does not match index: " +
                                expected.sha1()
                );
            }

            assets.add(mapAssetEntry(prepared));
        }

        if (!objectsBySha1.isEmpty()) {
            throw new IllegalArgumentException(
                    "Prepared asset objects are not declared in index: " +
                            objectsBySha1.keySet()
            );
        }

        return new AssetsIndex(assets, indexMetadata);
    }

    private Map<String, PreparedVanillaAssetObject> indexBySha1(
            List<PreparedVanillaAssetObject> objects
    ) {
        Map<String, PreparedVanillaAssetObject> objectsBySha1 = new HashMap<>();

        for (PreparedVanillaAssetObject prepared : objects) {
            Objects.requireNonNull(prepared, "prepared");

            String sha1 = prepared.verified().object().sha1();

            PreparedVanillaAssetObject previous = objectsBySha1.putIfAbsent(sha1, prepared);

            if (previous != null) {
                throw new IllegalArgumentException(
                        "Duplicate prepared asset object: " + sha1
                );
            }
        }

        return objectsBySha1;
    }

    private AssetIndexMetadata mapIndexMetadata(
            String indexId,
            PreparedVanillaAssetIndex index
    ) {
        VerifiedVanillaAssetIndex verified = index.verified();

        return new AssetIndexMetadata(
                indexId,
                "assets/indexes/" + indexId + ".json",
                verified.sha256(),
                verified.size(),
                index.uri().toString()
        );
    }

    private AssetEntry mapAssetEntry(PreparedVanillaAssetObject prepared) {
        VerifiedVanillaAssetObject verified = prepared.verified();
        VanillaAssetObject object = verified.object();
        String sha1 = object.sha1();

        String path = "assets/objects/"
                + sha1.substring(0, 2)
                + "/"
                + sha1;

        return new AssetEntry(
                path,
                verified.sha256(),
                object.size(),
                prepared.uri().toString()
        );
    }
}
