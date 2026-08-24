package com.launcher.verification.service;

import com.launcher.core.verification.model.ResourceVerificationResult;
import com.launcher.core.verification.model.VerificationPlan;
import com.launcher.core.verification.model.VerificationStatus;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.ManifestResources;
import com.launcher.model.manifest.ResourceEntry;
import com.launcher.verification.support.FixedDirectoryProvider;
import com.launcher.verification.support.RecordingFileVerifier;
import com.launcher.verification.support.RecordingManifest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultVerificationServiceTest {
    private final RecordingManifest recordingManifest = new RecordingManifest();
    private DefaultVerificationService defaultVerificationService;
    private RecordingFileVerifier recordingFileVerifier;

    @BeforeEach
    void setUp() {
        recordingFileVerifier = new RecordingFileVerifier();
        FixedDirectoryProvider fixedDirectoryProvider = new FixedDirectoryProvider(
                Path.of("launcher-directory")
        );

        defaultVerificationService = new DefaultVerificationService(
                fixedDirectoryProvider, recordingFileVerifier
        );
    }

    @Test
    void should_resolve_resource_path_against_launcher_directory() {
        //given
        Path launcherDirectory = Path.of("launcher-directory");
        Manifest manifest = recordingManifest.getManifest();

        List<ResourceEntry> resources = ManifestResources.from(manifest);
        List<Path> expectedPaths = resources.stream()
                .map(resource -> launcherDirectory.resolve(resource.path()))
                .toList();

        //when
        defaultVerificationService.verify(manifest);

        //then
        assertEquals(expectedPaths, recordingFileVerifier.getPaths());
    }

    @Test
    void should_return_valid_plan_when_all_resources_are_valid() {
        //given
        Manifest manifest = recordingManifest.getManifest();

        //when
        VerificationPlan verificationPlan = defaultVerificationService.verify(manifest);

        //then
        assertTrue(verificationPlan.isValid());

    }

    @Test
    void should_verify_files_and_libraries_from_manifest_resources() {
        //given
        Manifest manifest = recordingManifest.getManifest();
        recordingFileVerifier.setVerificationStatus(VerificationStatus.CORRUPTED);

        //when
        VerificationPlan verificationPlan = defaultVerificationService.verify(manifest);

        //then
        List<ResourceVerificationResult> expectedVerificationResults = recordingFileVerifier.getResourceEntries().stream()
                .map(this::getVerificationResult)
                .toList();

        VerificationPlan expectedVerificationPlan = new VerificationPlan(expectedVerificationResults);

        assertEquals(expectedVerificationPlan, verificationPlan);

    }

    private ResourceVerificationResult getVerificationResult(ResourceEntry resourceEntry) {
        return new ResourceVerificationResult(
                resourceEntry,
                recordingFileVerifier.getVerificationStatus()
        );
    }

}
