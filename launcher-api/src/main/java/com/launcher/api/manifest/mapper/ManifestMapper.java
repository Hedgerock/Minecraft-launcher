package com.launcher.api.manifest.mapper;

import com.launcher.model.manifest.Manifest;

public interface ManifestMapper {

    Manifest map(String json);

}
