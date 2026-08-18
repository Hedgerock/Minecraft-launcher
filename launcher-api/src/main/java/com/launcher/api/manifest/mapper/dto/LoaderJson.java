package com.launcher.api.manifest.mapper.dto;

import com.launcher.model.manifest.LoaderInfo;

public record LoaderJson(
        String type,
        String version
) {

    LoaderInfo toLoaderInfo() {
        return new LoaderInfo(type, version);
    }

}
