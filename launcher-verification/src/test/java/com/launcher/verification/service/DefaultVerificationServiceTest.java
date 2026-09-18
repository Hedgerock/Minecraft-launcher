package com.launcher.verification.service;

import com.launcher.core.resource.ResourcePathResolver;
import com.launcher.core.resource.ResourceSetConflictException;
import com.launcher.core.resource.ResourceSetPlanner;
import com.launcher.core.resource.SafeResourcePathResolver;
import com.launcher.core.verification.VerificationService;
import com.launcher.core.verification.model.ResourceVerificationResult;
import com.launcher.core.verification.model.VerificationPlan;
import com.launcher.core.verification.model.VerificationStatus;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.ManifestResources;
import com.launcher.model.manifest.ResourceEntry;
import com.launcher.verification.support.FixedDirectoryProvider;
import com.launcher.verification.support.RecordingFileVerifier;
import com.launcher.verification.support.RecordingManifest;
import com.launcher.verification.support.RecordingResourcePathResolver;
import com.launcher.verification.support.model.TestResourcePathResolverRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultVerificationServiceTest {
    private final RecordingManifest recordingManifest = new RecordingManifest();
    private DefaultVerificationService defaultVerificationService;
    private RecordingFileVerifier recordingFileVerifier;
    private RecordingResourcePathResolver resourcePathResolver;
    private FixedDirectoryProvider fixedDirectoryProvider;
    private final Path resolvedPath = Path.of("resolved/test-file.jar");

    @BeforeEach
    void setUp() {
        recordingFileVerifier = new RecordingFileVerifier();
        resourcePathResolver =
                new RecordingResourcePathResolver(resolvedPath);

        fixedDirectoryProvider = new FixedDirectoryProvider(
                Path.of("launcher-directory"),
                Path.of("game-directory")
        );

        ResourceSetPlanner resourceSetPlanner = new ResourceSetPlanner(resourcePathResolver);

        defaultVerificationService = new DefaultVerificationService(
                fixedDirectoryProvider,
                recordingFileVerifier,
                resourceSetPlanner
        );
    }

    @Test
    void should_fail_when_paths_resolve_to_same_target_but_resources_are_incompatible() {
        //given
        ResourcePathResolver resourcePathResolver = new SafeResourcePathResolver();
        VerificationService verificationService = new DefaultVerificationService(
                fixedDirectoryProvider,
                recordingFileVerifier,
                new ResourceSetPlanner(resourcePathResolver)
        );

        Manifest manifest = recordingManifest.getManifestWithIncompatibleResources();

        //when & then
        ResourceSetConflictException exception = assertThrows(
                ResourceSetConflictException.class,
                () -> verificationService.verify(manifest)
        );

        ResourceEntry firstResource = new ResourceEntry(
                "path/same-path.jar",
                "same-sha256",
                123L,
                "http://localhost/files/same-path.jar"
        );

        ResourceEntry conflictingResource = new ResourceEntry(
                "path/./same-path.jar",
                "same-sha256",
                312L,
                "http://localhost/files/same-path.jar"
        );

        Path expectedPath = resourcePathResolver.resolve(
                fixedDirectoryProvider.directories().game(),
                firstResource.path()
        );

        assertEquals(
                expectedPath,
                exception.getTargetPath()
        );

        assertEquals(
                firstResource,
                exception.getFirstResource()
        );

        assertEquals(
                conflictingResource,
                exception.getConflictingResource()
        );

        String expectedMessage = "Conflicting resources for target '%s': '%s' and '%s'".formatted(
                expectedPath,
                firstResource.path(),
                conflictingResource.path()
        );

        assertEquals(
                expectedMessage,
                exception.getMessage()
        );

        assertTrue(recordingFileVerifier.getPaths().isEmpty());
        assertTrue(recordingFileVerifier.getResourceEntries().isEmpty());
    }

    @Test
    void should_merge_compatible_resources_when_paths_resolve_to_same_target() {
        //given
        VerificationService verificationService = new DefaultVerificationService(
                fixedDirectoryProvider,
                recordingFileVerifier,
                new ResourceSetPlanner(
                        new SafeResourcePathResolver()
                )
        );

        Manifest manifest = recordingManifest.getManifestWithPotentialResolvedAsTheSameTarget();

        //when
        VerificationPlan verificationPlan = verificationService.verify(manifest);

        //then
        List<ResourceEntry> expectedResources = List.of(
                new ResourceEntry(
                        "path/same-path.jar",
                        "same-sha256",
                        123L,
                        "http://localhost/files/same-path.jar"
                ),
                new ResourceEntry(
                        "libraries/org/example/example.jar",
                        "sha256-library",
                        123L,
                        "http://localhost/libraries/example.jar"
                )
        );

        assertEquals(
                expectedResources,
                verificationPlan.resources().stream().map(ResourceVerificationResult::resource).toList()
        );

        List<Path> expectedPaths = expectedResources.stream()
                .map(resource -> fixedDirectoryProvider.directories().game()
                        .resolve(resource.path())
                        .normalize()
                )
                .toList();

        assertEquals(expectedPaths, recordingFileVerifier.getPaths());
    }

    @Test
    void should_keep_first_resource_when_duplicates_are_compatible() {
        //given
        VerificationService verificationService = new DefaultVerificationService(
                fixedDirectoryProvider,
                recordingFileVerifier,
                new ResourceSetPlanner(
                        new SafeResourcePathResolver()
                )
        );

        Manifest manifest = recordingManifest.getManifestWithTheSamePaths();

        //when
        VerificationPlan verificationPlan = verificationService.verify(manifest);

        //then
        List<ResourceEntry> expectedResources = List.of(
                new ResourceEntry(
                        "same-path.jar",
                        "same-sha256",
                        123L,
                        "http://localhost/files/same-path.jar"
                ),
                new ResourceEntry(
                        "libraries/org/example/example.jar",
                        "sha256-library",
                        123L,
                        "http://localhost/libraries/example.jar"
                )
        );

        assertEquals(
                expectedResources,
                verificationPlan.resources().stream().map(ResourceVerificationResult::resource).toList()
        );

        assertEquals(
                expectedResources,
                recordingFileVerifier.getResourceEntries()
        );

        List<Path> expectedPaths = expectedResources.stream()
                .map(resource -> fixedDirectoryProvider.directories().game()
                        .resolve(resource.path())
                        .normalize()
                )
                .toList();

        assertEquals(expectedPaths, recordingFileVerifier.getPaths());
    }

    @Test
    void should_pass_resolved_path_file_verifier() {
        //given
        Manifest manifest = recordingManifest.getManifestWithSingleResource();
        resourcePathResolver.setReturnResolvedPath(true);

        //when
        defaultVerificationService.verify(manifest);

        //then
        assertEquals(
                List.of(resolvedPath),
                recordingFileVerifier.getPaths()
        );
    }

    @Test
    void should_pass_game_directory_and_resource_path_to_resource_path_resolver() {
        //given
        Path gameDirectory = Path.of("game-directory");
        Manifest manifest = recordingManifest.getManifest();

        List<ResourceEntry> resources = ManifestResources.from(manifest);
        List<TestResourcePathResolverRecord> expectedCalls = resources.stream()
                .map(resource ->
                        new TestResourcePathResolverRecord(
                                gameDirectory,
                                resource.path()
                        )
                )
                .toList();

        //when
        defaultVerificationService.verify(manifest);

        //then
        assertEquals(expectedCalls, resourcePathResolver.getResourcePathResolverRecords());
    }

    @Test
    void should_resolve_resource_path_against_game_directory() {
        //given
        Path gameDirectory = Path.of("game-directory");
        Manifest manifest = recordingManifest.getManifest();

        List<ResourceEntry> resources = ManifestResources.from(manifest);
        List<Path> expectedPaths = resources.stream()
                .map(resource -> gameDirectory.resolve(resource.path()))
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
