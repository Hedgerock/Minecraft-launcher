package com.launcher.core.architecture.verification.model;

import com.launcher.core.verification.model.ResourceVerificationResult;
import com.launcher.core.verification.model.VerificationStatus;
import com.launcher.model.manifest.ResourceEntry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceVerificationResultTest {

    @Test
    void should_reject_null_status() {
        //given
        ResourceEntry resourceEntry = new ResourceEntry(
                "test",
                "sha256",
                123L,
                "https://test.com"
        );

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new ResourceVerificationResult(
                        resourceEntry,
                        null
                )
        );

        assertTrue(exception.getMessage().contains("status"));
    }

    @Test
    void should_reject_null_resource() {
        //given
        VerificationStatus status = VerificationStatus.MISSING;

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new ResourceVerificationResult(
                        null,
                        status
                )
        );

        assertTrue(exception.getMessage().contains("resource"));
    }

}
