package com.launcher.preparer;

import com.launcher.preparer.model.VanillaAssetIndex;
import com.launcher.preparer.model.VanillaAssetObject;
import com.launcher.preparer.model.VerifiedVanillaAssetObject;
import com.launcher.preparer.verifier.VanillaAssetObjectVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class VanillaAssetObjectsPreparer {
    private final VanillaAssetObjectSource source;
    private final VanillaAssetObjectVerifier verifier;

    public VanillaAssetObjectsPreparer(
            VanillaAssetObjectSource source,
            VanillaAssetObjectVerifier verifier
    ) {
        this.source = Objects.requireNonNull(source, "source");
        this.verifier = Objects.requireNonNull(verifier, "verifier");
    }

    public List<VerifiedVanillaAssetObject> prepare(
            VanillaAssetIndex index
    ) throws IOException {
        Objects.requireNonNull(index, "index");

        List<VerifiedVanillaAssetObject> verifiedObjects = new ArrayList<>();

        for (VanillaAssetObject object : index.objects()) {
            try (InputStream inputStream = source.open(object)) {
                verifiedObjects.add(verifier.verify(
                        object,
                        inputStream
                ));
            }
        }

        return List.copyOf(verifiedObjects);
    }
}
