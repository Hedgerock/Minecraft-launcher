package com.launcher.api.manifest.mapper.dto;

import com.launcher.model.manifest.LaunchInfo;

import java.util.List;
import java.util.Objects;

public record LaunchInfoJson(
        String mainClass,
        List<String> jvmArgs,
        List<String> gameArgs,
        List<String> classpath,
        String javaExecutable,
        JavaVersionRequirementJson javaVersionRequirement
) {

    public LaunchInfoJson {
        Objects.requireNonNull(javaVersionRequirement, "javaVersionRequirement");
    }

    LaunchInfo toLaunchInfo() {
        return new LaunchInfo(
                mainClass,
                jvmArgs,
                gameArgs,
                classpath,
                javaExecutable,
                javaVersionRequirement.toJavaVersionRequirement()
        );
    }

}
