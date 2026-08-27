package com.launcher.model.manifest;

import com.launcher.model.manifest.rules.LibraryRule;
import com.launcher.model.manifest.rules.LibraryRuleAction;
import com.launcher.model.runtime.OperatingSystem;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RuntimeLibraryMetadataTest {

    @Test
    void should_reject_null_library_artifact_metadata() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new RuntimeLibraryMetadata(null, List.of())
        );

        assertTrue(exception.getMessage().contains("artifact"));
    }

    @Test
    void should_reject_library_rule_with_null_value() {
        LibraryArtifactMetadata artifact = getLibraryArtifactMetadata("value.jar");
        List<LibraryRule> rules = new ArrayList<>();
        rules.add(null);

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> new RuntimeLibraryMetadata(
                        artifact,
                        rules
                )
        );

    }

    @Test
    void should_allow_empty_library_rules() {
        //given
        LibraryArtifactMetadata artifact = getLibraryArtifactMetadata("value.jar");

        //when
        RuntimeLibraryMetadata metadata = new RuntimeLibraryMetadata(
                artifact,
                List.of()
        );

        //then
        assertTrue(metadata.rules().isEmpty());
    }

    @Test
    void should_reject_mutation_of_library_rules_from_accessor() {
        //given
        LibraryArtifactMetadata artifact = getLibraryArtifactMetadata("value.jar");
        List<LibraryRule> rules = new ArrayList<>();
        rules.add(getLibraryRule(LibraryRuleAction.ALLOW));

        RuntimeLibraryMetadata metadata = new RuntimeLibraryMetadata(artifact, rules);

        //when
        assertThrows(
                UnsupportedOperationException.class,
                () -> metadata.rules().add(getLibraryRule(LibraryRuleAction.DISALLOW))
        );
    }

    @Test
    void should_create_immutable_library_rules() {
        //given
        LibraryArtifactMetadata artifact = getLibraryArtifactMetadata("value.jar");
        List<LibraryRule> rules = new ArrayList<>();
        rules.add(getLibraryRule(LibraryRuleAction.ALLOW));

        RuntimeLibraryMetadata metadata = new RuntimeLibraryMetadata(
                artifact,
                rules
        );

        //when
        rules.add(getLibraryRule(LibraryRuleAction.DISALLOW));

        //then
        assertEquals(
                1,
                metadata.rules().size()
        );

        assertEquals(
                List.of(getLibraryRule(LibraryRuleAction.ALLOW)),
                metadata.rules()
        );

    }

    @Test
    void should_reject_null_library_rules() {
        //given
        LibraryArtifactMetadata metadata = getLibraryArtifactMetadata("test.jar");

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new RuntimeLibraryMetadata(metadata, null)
        );

        assertTrue(exception.getMessage().contains("rules"));
    }

    private LibraryRule getLibraryRule(
            LibraryRuleAction action
    ) {
        return new LibraryRule(
                action,
                OperatingSystem.WINDOWS
        );
    }

    private LibraryArtifactMetadata getLibraryArtifactMetadata(String path) {
        return new LibraryArtifactMetadata(
                path,
                "sha256-" + path,
                123L,
                "http://localhost/" + path + ".jar"
        );
    }

}
