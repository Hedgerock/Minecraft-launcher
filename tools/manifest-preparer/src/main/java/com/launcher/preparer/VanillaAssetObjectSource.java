package com.launcher.preparer;

import com.launcher.preparer.model.VanillaAssetObject;

import java.io.IOException;
import java.io.InputStream;

public interface VanillaAssetObjectSource {

    InputStream open(VanillaAssetObject object) throws IOException;
}
