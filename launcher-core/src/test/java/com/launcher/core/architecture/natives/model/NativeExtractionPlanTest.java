package com.launcher.core.architecture.natives.model;

import com.launcher.core.natives.model.NativeExtractionPlan;
import com.launcher.model.manifest.LibraryEntry;
import com.launcher.model.manifest.natives.NativeExtractionRules;
import com.launcher.model.manifest.natives.SelectedNativeArtifact;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativeExtractionPlanTest {
    private static final List<SelectedNativeArtifact> DEFAULT_ARTIFACTS = List.of(
            new SelectedNativeArtifact(
                    new LibraryEntry(
                            "path.jar",
                            "sha256",
                            123L,
                            "http://localhost/path.jar"
                    ),
                    new NativeExtractionRules(
                            List.of()
                    )
            )
    );

    private static final Path DEFAULT_TARGET_DIRECTORY =
            Path.of("default-target-directory");


    @Test
    void should_return_not_empty_when_artifacts_exist() {
        //when
        NativeExtractionPlan plan = new NativeExtractionPlan(DEFAULT_ARTIFACTS, DEFAULT_TARGET_DIRECTORY);

        //then
        assertFalse(plan.artifacts().isEmpty());
    }

    @Test
    void should_return_empty_when_artifacts_are_empty() {
        //given
        List<SelectedNativeArtifact> artifacts = new ArrayList<>();

        //when
        NativeExtractionPlan plan = new NativeExtractionPlan(artifacts, DEFAULT_TARGET_DIRECTORY);

        //then
        assertTrue(plan.artifacts().isEmpty());
    }

    @Test
    void should_reject_null_artifact_from_artifacts() {
        //given
        List<SelectedNativeArtifact> artifacts = new ArrayList<>();

        artifacts.add(null);

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> new NativeExtractionPlan(artifacts, DEFAULT_TARGET_DIRECTORY)
        );
    }

    @Test
    void should_reject_artifacts_mutation_from_accessor() {
        //given
        LibraryEntry candidate = new LibraryEntry(
                "candidate.jar",
                "candidate-sha256",
                123L,
                "http://localhost/candidate.jar"
        );

        List<String> exclude = List.of();
        NativeExtractionRules rules = new NativeExtractionRules(exclude);
        SelectedNativeArtifact artifactCandidate = new SelectedNativeArtifact(candidate, rules);

        List<SelectedNativeArtifact> artifacts = new ArrayList<>(DEFAULT_ARTIFACTS);

        NativeExtractionPlan plan = new NativeExtractionPlan(artifacts, DEFAULT_TARGET_DIRECTORY);

        //when & then
        assertThrows(
                UnsupportedOperationException.class,
                () -> plan.artifacts().add(artifactCandidate)
        );
    }

    @Test
    void should_create_immutable_list_of_artifacts() {
        //given
        LibraryEntry candidate = new LibraryEntry(
                "candidate.jar",
                "candidate-sha256",
                123L,
                "http://localhost/candidate.jar"
        );

        List<String> exclude = List.of();
        NativeExtractionRules rules = new NativeExtractionRules(exclude);
        SelectedNativeArtifact artifactCandidate = new SelectedNativeArtifact(candidate, rules);

        List<SelectedNativeArtifact> artifacts = new ArrayList<>(DEFAULT_ARTIFACTS);
        NativeExtractionPlan plan = new NativeExtractionPlan(artifacts, DEFAULT_TARGET_DIRECTORY);

        //when
        artifacts.add(artifactCandidate);

        //then
        assertEquals(
                DEFAULT_ARTIFACTS,
                plan.artifacts()
        );

    }

    @Test
    void should_reject_null_target_directory() {
        //when & then
        NullPointerException exception =
                assertThrows(
                        NullPointerException.class,
                        () -> new NativeExtractionPlan(DEFAULT_ARTIFACTS, null)
                );

        assertTrue(exception.getMessage().contains("targetDirectory"));
    }

    @Test
    void should_reject_null_artifacts() {
        //when & then
        NullPointerException exception =
                assertThrows(
                        NullPointerException.class,
                        () -> new NativeExtractionPlan(null, DEFAULT_TARGET_DIRECTORY)
                );

        assertTrue(exception.getMessage().contains("artifacts"));
    }

}
