package com.launcher.verification.file;

import com.launcher.model.manifest.ResourceEntry;
import com.launcher.storage.file.FileMetadataReader;
import com.launcher.storage.hash.HashService;
import com.launcher.core.verification.model.ResourceVerificationResult;
import com.launcher.core.verification.model.VerificationStatus;
import com.launcher.verification.support.FixedHashService;
import com.launcher.verification.support.FixedMetadataReader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultFileVerifierTest {

    private static final Path FILE_PATH = Path.of("test/a.jar");

    @Test
    void should_return_missing_when_file_does_not_exist() {
        //given
        FileMetadataReader metadataReader = new FixedMetadataReader(false, 123L);
        HashService hashService = new FixedHashService("expected-sha256");
        FileVerifier fileVerifier = new DefaultFileVerifier(metadataReader, hashService);

        //when
        ResourceVerificationResult result = fileVerifier.verify(FILE_PATH, resource());

        //then
        assertEquals(VerificationStatus.MISSING, result.status());
    }

    @Test
    void should_return_outdated_when_file_size_is_different() {
        //given
        FileMetadataReader metadataReader = new FixedMetadataReader(true, 999L);
        HashService hashService = new FixedHashService("expected-sha256");
        FileVerifier fileVerifier = new DefaultFileVerifier(metadataReader, hashService);

        //when
        ResourceVerificationResult result = fileVerifier.verify(FILE_PATH, resource());

        //then
        assertEquals(VerificationStatus.OUTDATED, result.status());
    }

    @Test
    void should_return_corrupted_when_hash_is_different() {
        //given
        FileMetadataReader metadataReader = new FixedMetadataReader(true, 123L);
        HashService hashService = new FixedHashService("actual-sha256");
        FileVerifier fileVerifier = new DefaultFileVerifier(metadataReader, hashService);

        //when
        ResourceVerificationResult result = fileVerifier.verify(FILE_PATH, resource());

        //then
        assertEquals(VerificationStatus.CORRUPTED, result.status());
    }

    @Test
    void should_return_valid_when_file_exists_size_and_hash_match() {
        //given
        FileMetadataReader metadataReader = new FixedMetadataReader(true, 123L);
        HashService hashService = new FixedHashService("expected-sha256");
        FileVerifier fileVerifier = new DefaultFileVerifier(metadataReader, hashService);

        //when
        ResourceVerificationResult result = fileVerifier.verify(FILE_PATH, resource());

        //then
        assertEquals(VerificationStatus.VALID, result.status());
    }

    private ResourceEntry resource() {
        return new ResourceEntry(
                "test/a.jar",
                "expected-sha256",
                123L,
                "https://example.com/test/a.jar"
        );
    }
}
