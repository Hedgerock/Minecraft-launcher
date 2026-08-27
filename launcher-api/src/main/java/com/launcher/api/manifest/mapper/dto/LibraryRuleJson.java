package com.launcher.api.manifest.mapper.dto;

import com.launcher.model.manifest.rules.LibraryRule;
import com.launcher.model.manifest.rules.LibraryRuleAction;
import com.launcher.model.runtime.OperatingSystem;

import java.util.Locale;

public record LibraryRuleJson(
        String action,
        String os
) {
    LibraryRule toLibraryRule() {
        return new LibraryRule(
                LibraryRuleAction.valueOf(action.toUpperCase(Locale.ROOT)),
                OperatingSystem.valueOf(os.toUpperCase(Locale.ROOT))
        );
    }
}
