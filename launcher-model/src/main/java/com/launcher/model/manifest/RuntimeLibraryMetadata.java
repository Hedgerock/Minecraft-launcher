package com.launcher.model.manifest;

import com.launcher.model.manifest.classifiers.LibraryClassifiersMetadata;
import com.launcher.model.manifest.natives.LibraryNativesMetadata;
import com.launcher.model.manifest.natives.NativeExtractionRules;
import com.launcher.model.manifest.rules.LibraryRule;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record RuntimeLibraryMetadata(
        LibraryArtifactMetadata artifact,
        List<LibraryRule> rules,
        LibraryClassifiersMetadata classifiers,
        LibraryNativesMetadata natives,
        NativeExtractionRules extractionRules
) {

    public RuntimeLibraryMetadata(
            LibraryArtifactMetadata artifact,
            List<LibraryRule> rules
    ) {
        this(
                artifact,
                rules,
                new LibraryClassifiersMetadata(Map.of()),
                new LibraryNativesMetadata(Map.of()),
                new NativeExtractionRules(List.of())
        );
    }

    public RuntimeLibraryMetadata {
        Objects.requireNonNull(artifact, "artifact");
        Objects.requireNonNull(rules, "rules");
        Objects.requireNonNull(classifiers, "classifiers");
        Objects.requireNonNull(natives, "natives");
        Objects.requireNonNull(extractionRules, "extractionRules");

        rules = List.copyOf(rules);
    }

}
