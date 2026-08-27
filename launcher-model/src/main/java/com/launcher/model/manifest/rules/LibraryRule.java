package com.launcher.model.manifest.rules;

import com.launcher.model.runtime.OperatingSystem;

import java.util.Objects;

public record LibraryRule(
        LibraryRuleAction action,
        OperatingSystem operatingSystem
) {

    public LibraryRule {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(operatingSystem, "operatingSystem");
    }

}
