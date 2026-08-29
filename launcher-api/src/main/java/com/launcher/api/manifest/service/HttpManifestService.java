package com.launcher.api.manifest.service;

import com.launcher.api.manifest.client.ManifestClient;
import com.launcher.api.manifest.mapper.ManifestMapper;
import com.launcher.core.manifest.ManifestService;
import com.launcher.model.manifest.ManifestLoadResult;

public class HttpManifestService implements ManifestService {

    private final ManifestClient manifestClient;
    private final ManifestMapper manifestMapper;

    public HttpManifestService(ManifestClient manifestClient, ManifestMapper manifestMapper) {
        this.manifestClient = manifestClient;
        this.manifestMapper = manifestMapper;
    }

    @Override
    public ManifestLoadResult loadManifest() {
        String json = this.manifestClient.download();

        return manifestMapper.map(json);
    }
}
