package com.launcher.core.architecture.verification.model;

import com.launcher.core.verification.model.FileVerificationResult;
import com.launcher.core.verification.model.VerificationStatus;
import com.launcher.model.manifest.FileEntry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileVerificationResultTest {

    @Test
    void should_reject_null_status() {
        //given
        FileEntry fileEntry = new FileEntry(
                "test",
                "sha256",
                123L,
                "https://test.com"
        );

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new FileVerificationResult(
                        fileEntry,
                        null
                )
        );

        assertTrue(exception.getMessage().contains("status"));
    }

    @Test
    void should_reject_null_file() {
        //given
        VerificationStatus status = VerificationStatus.MISSING;

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new FileVerificationResult(
                        null,
                        status
                )
        );

        assertTrue(exception.getMessage().contains("file"));
    }

}
