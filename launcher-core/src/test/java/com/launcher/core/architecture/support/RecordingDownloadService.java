package com.launcher.core.architecture.support;

import com.launcher.core.download.DownloadPlan;
import com.launcher.core.download.DownloadService;

public class RecordingDownloadService implements DownloadService {
    private boolean withError = false;
    private DownloadPlan downloadPlan;

    public RecordingDownloadService() {
    }

    public RecordingDownloadService(boolean withError) {
        this.withError = withError;
    }

    @Override
    public void download(DownloadPlan plan) {
        this.downloadPlan = plan;

        if (withError) {
            throw new IllegalStateException("Download failed");
        }

    }

    public DownloadPlan getDownloadPlan() {
        return downloadPlan;
    }
}
