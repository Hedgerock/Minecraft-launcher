package com.launcher.api.manifest.mapper.dto;

import com.launcher.model.manifest.LibraryArtifactMetadata;
import com.launcher.model.manifest.RuntimeLibraryMetadata;
import com.launcher.model.manifest.classifiers.LibraryClassifiersMetadata;
import com.launcher.model.manifest.natives.LibraryNativesMetadata;
import com.launcher.model.runtime.OperatingSystem;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public record LibraryEntryJson(
        String path,
        String sha256,
        long size,
        String url,
        List<LibraryRuleJson> rules,
        Map<String, LibraryArtifactJson> classifiers,
        Map<String, String> natives
) {

    public LibraryEntryJson {
        rules = rules == null
                ? List.of()
                : List.copyOf(rules);

        classifiers = classifiers == null
                ? Map.of()
                : Map.copyOf(classifiers);

        natives = natives == null
                ? Map.of()
                : Map.copyOf(natives);
    }

    RuntimeLibraryMetadata toRuntimeLibraryMetadata() {
        return new RuntimeLibraryMetadata(
                toLibraryArtifactMetadata(),
                rules.stream()
                        .map(LibraryRuleJson::toLibraryRule)
                        .toList(),
                toLibraryClassifiers(),
                toLibraryNatives()
        );
    }

    private LibraryClassifiersMetadata toLibraryClassifiers() {
        return new LibraryClassifiersMetadata(
                classifiers.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().toLibraryArtifactMetadata()
                        ))
        );
    }

    private LibraryNativesMetadata toLibraryNatives() {
        return new LibraryNativesMetadata(
                natives.entrySet().stream()
                        .collect(Collectors.toMap(
                                entry ->
                                        OperatingSystem.valueOf(entry.getKey().toUpperCase(Locale.ROOT)),
                                Map.Entry::getValue
                        ))
        );
    }

    private LibraryArtifactMetadata toLibraryArtifactMetadata() {
        return new LibraryArtifactMetadata(
                path,
                sha256,
                size,
                url
        );
    }
}
