package com.launcher.model.manifest.classifiers;

import com.launcher.model.manifest.LibraryArtifactMetadata;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibraryClassifiersMetadataTest {

    @Test
    void should_create_immutable_artifacts() {
        //given
        Map<String, LibraryArtifactMetadata> artifacts = new HashMap<>();
        artifacts.put("default-value", getDefaultArtifact());
        LibraryClassifiersMetadata classifiersMetadata = new LibraryClassifiersMetadata(
                artifacts
        );

        //when
        artifacts.put("key", getArtifact("new-path"));

        //then
        Map<String, LibraryArtifactMetadata>
                expectedArtifacts = Map.of(
                        "default-value", getDefaultArtifact()
        );

        assertEquals(
                expectedArtifacts,
                classifiersMetadata.artifacts()
        );
    }

    @Test
    void should_reject_mutation_of_artifacts_from_accessor() {
        //given
        Map<String, LibraryArtifactMetadata> artifacts = new HashMap<>();
        artifacts.put("default-value", getDefaultArtifact());
        LibraryClassifiersMetadata classifiersMetadata = new LibraryClassifiersMetadata(
                artifacts
        );

        //when & then
        assertThrows(
                UnsupportedOperationException.class,
                () -> classifiersMetadata.artifacts().put("key", getArtifact("another-path"))
        );
    }

    @Test
    void should_reject_null_classifier_artifact() {
        //given
        Map<String, LibraryArtifactMetadata> artifacts = new HashMap<>();
        artifacts.put("default-value", null);

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LibraryClassifiersMetadata(artifacts)
        );

        assertTrue(exception.getMessage().contains("artifact"));
    }

    @Test
    void should_reject_blank_classifier_name() {
        //given
        Map<String, LibraryArtifactMetadata> artifacts = new HashMap<>();
        artifacts.put(" ", getDefaultArtifact());

        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LibraryClassifiersMetadata(artifacts)
        );

        assertTrue(exception.getMessage().contains("classifierName must not be blank"));
    }

    @Test
    void should_reject_null_classifier_name() {
        //given
        Map<String, LibraryArtifactMetadata> artifacts = new HashMap<>();
        artifacts.put(null, getDefaultArtifact());

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LibraryClassifiersMetadata(artifacts)
        );

        assertTrue(exception.getMessage().contains("classifierName"));
    }

    @Test
    void should_allow_empty_artifacts() {
        //given & when
        Map<String, LibraryArtifactMetadata> artifacts = new HashMap<>();
        LibraryClassifiersMetadata classifiersMetadata = new LibraryClassifiersMetadata(
                artifacts
        );

        //then
        assertTrue(classifiersMetadata.isEmpty());
    }

    @Test
    void should_reject_null_artifacts() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LibraryClassifiersMetadata(null)
        );

        assertTrue(exception.getMessage().contains("artifacts"));
    }

    private LibraryArtifactMetadata getArtifact(String path) {
        return new LibraryArtifactMetadata(
                path,
                "sha256-" + path,
                123L,
                "http://localhost/" + path + ".jar"
        );
    }

    private LibraryArtifactMetadata getDefaultArtifact() {
        return new LibraryArtifactMetadata(
                "test-path",
                "test-sha256",
                123L,
                "test-url"
        );
    }

}
