package com.launcher.preparer;

import java.util.List;
import java.util.Objects;

public record VanillaAssetIndex(
        List<VanillaAssetObject> objects
) {

    public VanillaAssetIndex {
        Objects.requireNonNull(objects, "objects");

        objects = List.copyOf(objects);
    }
}
