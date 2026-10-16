package com.launcher.preparer;

import com.launcher.preparer.model.VanillaAssetObject;
import com.launcher.preparer.source.OpenedVanillaAssetResource;

import java.io.IOException;

public interface VanillaAssetObjectSource {

    OpenedVanillaAssetResource open(VanillaAssetObject object) throws IOException;
}
