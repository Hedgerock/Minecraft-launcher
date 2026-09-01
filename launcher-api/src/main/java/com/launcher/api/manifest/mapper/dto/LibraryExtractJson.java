package com.launcher.api.manifest.mapper.dto;

import com.launcher.model.manifest.natives.NativeExtractionRules;

import java.util.List;

public record LibraryExtractJson(
        List<String> exclude
) {

    public LibraryExtractJson {
        exclude = exclude == null
                ? List.of()
                : List.copyOf(exclude);
    }

    NativeExtractionRules toNativeExtractionRules() {
        return new NativeExtractionRules(exclude);
    }

}
