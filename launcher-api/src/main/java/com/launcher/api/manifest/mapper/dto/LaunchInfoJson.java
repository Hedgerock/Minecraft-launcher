package com.launcher.api.manifest.mapper.dto;

import com.launcher.model.manifest.LaunchInfo;

import java.util.List;

public record LaunchInfoJson(
        String mainClass,
        List<String> jvmArgs,
        List<String> gameArgs,
        List<String> classpath
) {

    LaunchInfo toLaunchInfo() {
        return new LaunchInfo(
                mainClass,
                jvmArgs,
                gameArgs,
                classpath
        );
    }

}
