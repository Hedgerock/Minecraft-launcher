package com.launcher.api.manifest.mapper;

import com.launcher.model.manifest.ManifestLoadResult;

public interface ManifestMapper {

    ManifestLoadResult map(String json);

}
