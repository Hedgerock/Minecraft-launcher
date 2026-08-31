package com.launcher.app.service;

import com.launcher.core.natives.NativeExtractionService;
import com.launcher.core.natives.model.NativeExtractionPlan;

public final class UnsupportedNativeExtractionService implements NativeExtractionService {

    @Override
    public void extract(NativeExtractionPlan plan) {
        throw new UnsupportedOperationException("Native extraction is not implemented");
    }
}
