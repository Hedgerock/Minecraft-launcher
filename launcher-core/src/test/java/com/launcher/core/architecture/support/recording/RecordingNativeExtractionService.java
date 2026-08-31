package com.launcher.core.architecture.support.recording;

import com.launcher.core.natives.NativeExtractionService;
import com.launcher.core.natives.model.NativeExtractionPlan;

public final class RecordingNativeExtractionService implements NativeExtractionService {
    private NativeExtractionPlan nativeExtractionPlan;
    private boolean withError = false;
    private String errorMessage;

    @Override
    public void extract(NativeExtractionPlan plan) {
        if (withError) {
            throw new IllegalStateException(errorMessage);
        }

        this.nativeExtractionPlan = plan;
    }

    public NativeExtractionPlan getNativeExtractionPlan() {
        return nativeExtractionPlan;
    }

    public void setWithError(String errorMessage) {
        this.errorMessage = errorMessage;

        if (withError) {
            return;
        }

        this.withError = true;
    }

}
