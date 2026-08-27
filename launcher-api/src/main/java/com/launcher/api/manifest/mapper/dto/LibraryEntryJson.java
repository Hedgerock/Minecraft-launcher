package com.launcher.api.manifest.mapper.dto;

import com.launcher.model.manifest.LibraryArtifactMetadata;
import com.launcher.model.manifest.RuntimeLibraryMetadata;

import java.util.List;

public record LibraryEntryJson(
        String path,
        String sha256,
        long size,
        String url,
        List<LibraryRuleJson> rules
) {

    public LibraryEntryJson {
        rules = rules == null
                ? List.of()
                : List.copyOf(rules);
    }

    RuntimeLibraryMetadata toRuntimeLibraryMetadata() {
        return new RuntimeLibraryMetadata(
                toLibraryArtifactMetadata(),
                rules.stream()
                        .map(LibraryRuleJson::toLibraryRule)
                        .toList()
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
