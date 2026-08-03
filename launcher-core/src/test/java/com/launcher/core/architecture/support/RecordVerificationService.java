package com.launcher.core.architecture.support;

import com.launcher.core.verification.VerificationService;
import com.launcher.core.verification.model.VerificationPlan;
import com.launcher.model.manifest.Manifest;

public class RecordVerificationService implements VerificationService {
    private final VerificationPlan verificationPlan;

    public RecordVerificationService(VerificationPlan verificationPlan) {
        this.verificationPlan = verificationPlan;
    }

    @Override
    public VerificationPlan verify(Manifest manifest) {
        return verificationPlan;
    }
}
