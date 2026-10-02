package com.launcher.api.manifest.support;

import com.launcher.api.manifest.mapper.ManifestMapper;
import com.launcher.model.manifest.ManifestLoadResult;

public final class RecordingManifestMapper implements ManifestMapper {
    private String json;
    private boolean wasCalled = false;

    @Override
    public ManifestLoadResult map(String json) {
        this.json = json;
        this.wasCalled = true;
        return null;
    }

    public String getJson() {
        return json;
    }

    public boolean wasCalled() {
        return wasCalled;
    }
}
