package com.launcher.api.manifest.mapper;


import com.launcher.model.manifest.LoaderInfo;
import com.launcher.model.manifest.Manifest;

import java.util.List;

public class JsonManifestMapper implements ManifestMapper {

    @Override
    public Manifest map(String json) {
        return new Manifest(
                "1.21.1",
                new LoaderInfo(
                        "fabric",
                        "0.16.10"
                ),
                List.of()
        );
    }
}
