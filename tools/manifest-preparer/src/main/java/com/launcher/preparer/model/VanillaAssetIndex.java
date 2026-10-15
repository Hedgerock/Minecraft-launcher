package com.launcher.preparer.model;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record VanillaAssetIndex(
        List<VanillaAssetObject> objects
) {

    public VanillaAssetIndex {
        Objects.requireNonNull(objects, "objects");

        Set<String> uniqueHashes = new HashSet<>();

        for (VanillaAssetObject object : objects) {
            Objects.requireNonNull(object, "object");

            if (!uniqueHashes.add(object.sha1())) {
                throw new IllegalArgumentException(
                        "Duplicate sha1 values found in objects"
                );
            }
        }

        objects = List.copyOf(objects);
    }
}
