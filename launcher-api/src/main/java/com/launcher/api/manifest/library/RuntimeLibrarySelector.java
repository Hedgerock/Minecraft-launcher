package com.launcher.api.manifest.library;

import com.launcher.model.manifest.LibraryEntry;
import com.launcher.model.manifest.RuntimeLibraryMetadata;

import java.util.List;

public interface RuntimeLibrarySelector {

    List<LibraryEntry> select(List<RuntimeLibraryMetadata> libraries);

}
