package com.launcher.model.manifest;

import com.launcher.model.manifest.classifiers.LibraryClassifiersMetadata;
import com.launcher.model.manifest.natives.LibraryNativesMetadata;
import com.launcher.model.manifest.rules.LibraryRule;
import com.launcher.model.manifest.rules.LibraryRuleAction;
import com.launcher.model.runtime.OperatingSystem;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RuntimeLibraryMetadataTest {

    @Test
    void should_store_natives_metadata() {
        //given && when
        LibraryArtifactMetadata classifierArtifact = getLibraryArtifactMetadata("classifier-artifact");
        LibraryClassifiersMetadata classifiers = new LibraryClassifiersMetadata(
                Map.of("classifier-value", classifierArtifact)
        );

        LibraryNativesMetadata natives = new LibraryNativesMetadata(
                Map.of(OperatingSystem.WINDOWS, "classifier-for-windows")
        );

        RuntimeLibraryMetadata runtimeLibraryMetadata = new RuntimeLibraryMetadata(
                getLibraryArtifactMetadata("artifact"),
                List.of(),
                classifiers,
                natives
        );

        //then
        assertFalse(runtimeLibraryMetadata.natives().isEmpty());
        assertEquals(1, runtimeLibraryMetadata.natives().classifiers().size());

        assertEquals(natives, runtimeLibraryMetadata.natives());
    }

    @Test
    void should_store_classifiers_metadata() {
        //given && when
        LibraryArtifactMetadata classifierArtifact = getLibraryArtifactMetadata("classifier-artifact");
        LibraryClassifiersMetadata classifiers = new LibraryClassifiersMetadata(
                Map.of("classifier-value", classifierArtifact)
        );

        LibraryNativesMetadata natives = new LibraryNativesMetadata(
                Map.of(OperatingSystem.WINDOWS, "classifier-for-windows")
        );

        RuntimeLibraryMetadata runtimeLibraryMetadata = new RuntimeLibraryMetadata(
                getLibraryArtifactMetadata("artifact"),
                List.of(),
                classifiers,
                natives
        );

        //then
        assertFalse(runtimeLibraryMetadata.classifiers().isEmpty());
        assertEquals(1, runtimeLibraryMetadata.classifiers().artifacts().size());

        assertEquals(classifiers, runtimeLibraryMetadata.classifiers());
    }

    @Test
    void should_use_empty_classifiers_and_natives_by_default() {
        //given && when
        RuntimeLibraryMetadata runtimeLibraryMetadata = new RuntimeLibraryMetadata(
                getLibraryArtifactMetadata("artifact"),
                List.of()
        );

        //then
        assertTrue(runtimeLibraryMetadata.classifiers().isEmpty());
        assertTrue(runtimeLibraryMetadata.natives().isEmpty());
    }

    @Test
    void should_reject_null_natives_metadata() {
        //given
        Map<String, LibraryArtifactMetadata> classifiers = Map.of(
                "classifier-for-windows", getLibraryArtifactMetadata("value")
        );

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () ->
                        new RuntimeLibraryMetadata(
                                getLibraryArtifactMetadata("library-artifact"),
                                List.of(),
                                new LibraryClassifiersMetadata(classifiers),
                                null
                        )
        );

        assertTrue(exception.getMessage().contains("natives"));
    }

    @Test
    void should_reject_null_classifiers_metadata() {
        //given
        Map<OperatingSystem, String> natives = Map.of(
                OperatingSystem.WINDOWS, "classifier-for-windows"
        );

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () ->
                        new RuntimeLibraryMetadata(
                                getLibraryArtifactMetadata("value"),
                                List.of(),
                                null,
                                new LibraryNativesMetadata(natives)
                        )
        );

        assertTrue(exception.getMessage().contains("classifiers"));
    }

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
        LibraryArtifactMetadata artifact = getLibraryArtifactMetadata("value");
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
        LibraryArtifactMetadata artifact = getLibraryArtifactMetadata("value");

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
        LibraryArtifactMetadata artifact = getLibraryArtifactMetadata("value");
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
        LibraryArtifactMetadata artifact = getLibraryArtifactMetadata("value");
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
