package com.launcher.verification.file;

import com.launcher.core.verification.model.ResourceVerificationResult;
import com.launcher.model.manifest.ResourceEntry;

import java.nio.file.Path;

public interface FileVerifier {

    ResourceVerificationResult verify(Path filePath, ResourceEntry resourceEntry);

}
