package com.launcher.api.manifest.mapper.dto;

import com.launcher.model.runtime.JavaVersionRequirement;

public record JavaVersionRequirementJson(
        int minimumMajorVersion
) {

    JavaVersionRequirement toJavaVersionRequirement() {
        return new JavaVersionRequirement(minimumMajorVersion);
    }
}
