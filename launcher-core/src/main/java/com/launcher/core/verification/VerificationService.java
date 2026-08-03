package com.launcher.core.verification;


import com.launcher.core.verification.model.VerificationPlan;
import com.launcher.model.manifest.Manifest;

public interface VerificationService {

    VerificationPlan verify(Manifest manifest);

}
