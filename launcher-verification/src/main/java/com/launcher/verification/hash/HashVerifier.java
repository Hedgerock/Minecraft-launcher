package com.launcher.verification.hash;

import com.launcher.model.manifest.Manifest;
import com.launcher.verification.model.VerificationStatus;

public interface HashVerifier {

    VerificationStatus verify(Manifest manifest);

}
