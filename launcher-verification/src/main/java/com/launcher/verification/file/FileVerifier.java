package com.launcher.verification.file;

import com.launcher.model.manifest.FileEntry;
import com.launcher.verification.model.FileVerificationResult;

import java.nio.file.Path;

public interface FileVerifier {

    FileVerificationResult verify(Path filePath, FileEntry file);

}
