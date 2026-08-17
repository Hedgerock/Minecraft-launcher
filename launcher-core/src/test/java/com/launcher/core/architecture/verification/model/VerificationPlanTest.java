package com.launcher.core.architecture.verification.model;

import com.launcher.core.verification.model.FileVerificationResult;
import com.launcher.core.verification.model.VerificationPlan;
import com.launcher.core.verification.model.VerificationStatus;
import com.launcher.model.manifest.FileEntry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VerificationPlanTest {

    private FileEntry getFileEntry(String path) {
        return new FileEntry(
                path,
                "sha256-" + path,
                123L,
                "http://test-path/" + path
        );
    }
    private FileVerificationResult getFileVerificationResult(
            FileEntry fileEntry,
            VerificationStatus status
    ) {
        return new FileVerificationResult(
                fileEntry,
                status
        );
    }

    @Test
    void should_reject_null_result() {
        //given
        List<FileVerificationResult> verificationResults = new ArrayList<>();

        verificationResults
                .add(null);
        verificationResults
                .add(
                        getFileVerificationResult(
                                getFileEntry("file1.jar"),
                                VerificationStatus.MISSING
                        )
                );

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> new VerificationPlan(verificationResults)
        );
    }

    @Test
    void should_create_immutable_files() {
        //given
        List<FileVerificationResult> verificationResults = new ArrayList<>();

        verificationResults
                .add(
                        getFileVerificationResult(
                                getFileEntry("file1.jar"),
                                VerificationStatus.VALID
                        )
                );
        verificationResults
                .add(
                        getFileVerificationResult(
                                getFileEntry("file1.jar"),
                                VerificationStatus.MISSING
                        )
                );

        VerificationPlan verificationPlan =
                new VerificationPlan(verificationResults);

        //when & then
        assertThrows(
                UnsupportedOperationException.class,
                () -> verificationPlan.files().add(
                        getFileVerificationResult(
                                getFileEntry("file3.jar"),
                                VerificationStatus.CORRUPTED
                        )
                )
        );
    }

    @Test
    void should_reject_null_files() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new VerificationPlan(null)
        );

        assertTrue(exception.getMessage().contains("files"));
    }

}
