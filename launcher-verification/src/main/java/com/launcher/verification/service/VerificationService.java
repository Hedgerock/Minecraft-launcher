package com.launcher.verification.service;


import com.launcher.model.manifest.Manifest;
import com.launcher.verification.model.VerificationPlan;

public interface VerificationService {

    VerificationPlan verify(Manifest manifest);

}
