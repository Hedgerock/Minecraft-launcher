package com.launcher.verification.hash;

import com.launcher.model.manifest.FileEntry;
import com.launcher.verification.model.VerificationStatus;

import java.nio.file.Path;

public interface HashVerifier {

    VerificationStatus verify(Path filePath, FileEntry fileEntry);

}
