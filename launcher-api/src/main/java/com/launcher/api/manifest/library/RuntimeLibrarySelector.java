package com.launcher.api.manifest.library;

import com.launcher.model.manifest.RuntimeLibraryMetadata;
import com.launcher.model.manifest.RuntimeLibrarySelection;
import com.launcher.model.runtime.RuntimeEnvironment;

import java.util.List;

public interface RuntimeLibrarySelector {

    RuntimeLibrarySelection select(
            List<RuntimeLibraryMetadata> libraries,
            RuntimeEnvironment environment
    );

}
