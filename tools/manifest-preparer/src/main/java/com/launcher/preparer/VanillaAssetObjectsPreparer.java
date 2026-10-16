package com.launcher.preparer;

import com.launcher.preparer.model.PreparedVanillaAssetObject;
import com.launcher.preparer.model.VanillaAssetIndex;
import com.launcher.preparer.model.VanillaAssetObject;
import com.launcher.preparer.model.VerifiedVanillaAssetObject;
import com.launcher.preparer.source.OpenedVanillaAssetResource;
import com.launcher.preparer.verifier.VanillaAssetObjectVerifier;

import java.io.IOException;
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

    public List<PreparedVanillaAssetObject> prepare(
            VanillaAssetIndex index
    ) throws IOException {
        Objects.requireNonNull(index, "index");

        List<PreparedVanillaAssetObject> preparedObjects = new ArrayList<>();

        for (VanillaAssetObject object : index.objects()) {
            try (OpenedVanillaAssetResource resource = source.open(object)) {
                VerifiedVanillaAssetObject verified = verifier.verify(
                        object,
                        resource.getInput()
                );

                preparedObjects.add(
                        new PreparedVanillaAssetObject(
                                verified,
                                resource.getUri()
                        )
                );
            }
        }

        return List.copyOf(preparedObjects);
    }
}
