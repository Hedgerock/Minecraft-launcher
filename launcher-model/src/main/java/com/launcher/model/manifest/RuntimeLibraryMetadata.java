package com.launcher.model.manifest;

import com.launcher.model.manifest.rules.LibraryRule;

import java.util.List;
import java.util.Objects;

public record RuntimeLibraryMetadata(
        LibraryArtifactMetadata artifact,
        List<LibraryRule> rules
) {

    public RuntimeLibraryMetadata {
        Objects.requireNonNull(artifact, "artifact");
        Objects.requireNonNull(rules, "rules");

        rules = List.copyOf(rules);
    }

}
