package com.launcher.api.manifest.library;

import com.launcher.model.manifest.LibraryArtifactMetadata;
import com.launcher.model.manifest.LibraryEntry;
import com.launcher.model.manifest.RuntimeLibraryMetadata;
import com.launcher.model.manifest.classifiers.LibraryClassifiersMetadata;
import com.launcher.model.manifest.natives.LibraryNativesMetadata;
import com.launcher.model.manifest.rules.LibraryRule;
import com.launcher.model.manifest.rules.LibraryRuleAction;
import com.launcher.model.runtime.OperatingSystem;
import com.launcher.model.runtime.RuntimeEnvironment;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DefaultRuntimeLibrarySelectorTest {
    private final RuntimeLibrarySelector selector = new DefaultRuntimeLibrarySelector();
    private final RuntimeEnvironment environment = new RuntimeEnvironment(OperatingSystem.WINDOWS);

    @Test
    void should_fail_when_native_classifier_is_mapped_but_classifier_artifact_is_missing() {
        //given
        Map<String, LibraryArtifactMetadata> classifiers = Map.of(
                "natives-windows",
                new LibraryArtifactMetadata(
                        "native-windows-path.jar",
                        "sha256",
                        100L,
                        "https://example.com/native-windows-path.jar"
                )
        );

        Map<OperatingSystem, String> natives = Map.of(
                OperatingSystem.WINDOWS,
                "invalid-classifier"
        );

        List<RuntimeLibraryMetadata> libraries = List.of(
                getRuntimeLibraryMetadata(
                        "libraries/example.jar",
                        List.of(
                                new LibraryRule(LibraryRuleAction.ALLOW, OperatingSystem.WINDOWS),
                                new LibraryRule(LibraryRuleAction.DISALLOW, OperatingSystem.LINUX)
                        ),
                        classifiers,
                        natives
                )
        );

        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> selector.select(libraries, environment)
        );

        assertTrue(exception.getMessage().contains("Native classifier artifact not found: invalid-classifier"));
    }

    @Test
    void should_not_include_native_artifact_when_library_is_excluded_by_rules() {
        //given
        Map<String, LibraryArtifactMetadata> classifiers = Map.of(
                "natives-windows",
                new LibraryArtifactMetadata(
                        "native-windows-path.jar",
                        "sha256",
                        100L,
                        "https://example.com/native-windows-path.jar"
                )
        );

        Map<OperatingSystem, String> natives = Map.of(
                OperatingSystem.WINDOWS,
                "natives-windows"
        );

        List<RuntimeLibraryMetadata> libraries = List.of(
                getRuntimeLibraryMetadata(
                        "libraries/example.jar",
                        List.of(
                                new LibraryRule(LibraryRuleAction.ALLOW, OperatingSystem.WINDOWS),
                                new LibraryRule(LibraryRuleAction.DISALLOW, OperatingSystem.WINDOWS)
                        ),
                        classifiers,
                        natives
                )
        );

        //when
        List<LibraryEntry> result = selector.select(libraries, environment);

        //then
        assertTrue(result.isEmpty());
    }

    @Test
    void should_not_include_native_artifact_when_current_operating_system_is_not_mapped() {
        //given
        Map<String, LibraryArtifactMetadata> classifiers = Map.of(
                "natives-windows",
                new LibraryArtifactMetadata(
                        "native-windows-path.jar",
                        "sha256",
                        100L,
                        "https://example.com/native-windows-path.jar"
                )
        );

        Map<OperatingSystem, String> natives = Map.of(
                OperatingSystem.LINUX,
                "natives-linux"
        );

        List<RuntimeLibraryMetadata> libraries = List.of(
                getRuntimeLibraryMetadata(
                        "libraries/example.jar",
                        List.of(
                                new LibraryRule(LibraryRuleAction.ALLOW, OperatingSystem.WINDOWS),
                                new LibraryRule(LibraryRuleAction.DISALLOW, OperatingSystem.MACOS)
                        ),
                        classifiers,
                        natives
                )
        );

        //when
        List<LibraryEntry> result = selector.select(libraries, environment);

        //then
        assertEquals(
                List.of(
                        getLibraryEntry("libraries/example.jar")
                ),
                result
        );
    }

    @Test
    void should_include_native_artifact_for_current_operating_system() {
        //given
        Map<String, LibraryArtifactMetadata> classifiers = Map.of(
                "natives-windows",
                new LibraryArtifactMetadata(
                        "native-windows-path.jar",
                        "sha256",
                        100L,
                        "https://example.com/native-windows-path.jar"
                )
        );

        Map<OperatingSystem, String> natives = Map.of(
                OperatingSystem.WINDOWS,
                "natives-windows"
        );

        List<RuntimeLibraryMetadata> libraries = List.of(
                getRuntimeLibraryMetadata(
                        "libraries/example.jar",
                        List.of(
                                new LibraryRule(LibraryRuleAction.ALLOW, OperatingSystem.WINDOWS),
                                new LibraryRule(LibraryRuleAction.DISALLOW, OperatingSystem.LINUX)
                        ),
                        classifiers,
                        natives
                ),
                getRuntimeLibraryMetadata(
                        "libraries/example2.jar",
                        List.of(
                                new LibraryRule(LibraryRuleAction.ALLOW, OperatingSystem.WINDOWS),
                                new LibraryRule(LibraryRuleAction.DISALLOW, OperatingSystem.WINDOWS),
                                new LibraryRule(LibraryRuleAction.ALLOW, OperatingSystem.MACOS)
                        )
                )
        );

        //when
        List<LibraryEntry> result = selector.select(libraries, environment);

        //then
        assertEquals(
                List.of(
                        getLibraryEntry("libraries/example.jar"),
                        getLibraryEntry("native-windows-path.jar")
                ),
                result
        );
    }

    @Test
    void should_ignore_non_matching_rules_when_resolving_last_matching_rule() {
        //given
        List<RuntimeLibraryMetadata> libraries = List.of(
                getRuntimeLibraryMetadata(
                        "libraries/example.jar",
                        List.of(
                                new LibraryRule(LibraryRuleAction.ALLOW, OperatingSystem.WINDOWS),
                                new LibraryRule(LibraryRuleAction.DISALLOW, OperatingSystem.LINUX)
                        )
                ),
                getRuntimeLibraryMetadata(
                        "libraries/example2.jar",
                        List.of(
                                new LibraryRule(LibraryRuleAction.ALLOW, OperatingSystem.WINDOWS),
                                new LibraryRule(LibraryRuleAction.DISALLOW, OperatingSystem.LINUX),
                                new LibraryRule(LibraryRuleAction.ALLOW, OperatingSystem.MACOS)
                        )
                )
        );

        //when
        List<LibraryEntry> result = selector.select(libraries, environment);

        //then
        assertEquals(
                List.of(
                        getLibraryEntry("libraries/example.jar"),
                        getLibraryEntry("libraries/example2.jar")
                ),
                result
        );
    }

    @Test
    void should_include_library_without_rules() {
        //given
        List<RuntimeLibraryMetadata> libraries = List.of(
                getRuntimeLibraryMetadata("libraries/example.jar"),
                getRuntimeLibraryMetadata("libraries/example2.jar")
        );

        //when
        List<LibraryEntry> result = selector.select(libraries, environment);

        //then
        assertEquals(
                List.of(
                        getLibraryEntry("libraries/example.jar"),
                        getLibraryEntry("libraries/example2.jar")
                ),
                result
        );
    }

    @Test
    void should_include_library_when_last_matching_rule_allows_current_os() {
        //given
        List<RuntimeLibraryMetadata> libraries = List.of(
                getRuntimeLibraryMetadata(
                        "libraries/example.jar",
                        List.of(
                                new LibraryRule(LibraryRuleAction.DISALLOW, OperatingSystem.WINDOWS),
                                new LibraryRule(LibraryRuleAction.ALLOW, OperatingSystem.WINDOWS)
                        )
                ),
                getRuntimeLibraryMetadata("libraries/example2.jar")
        );

        //when
        List<LibraryEntry> result = selector.select(libraries, environment);

        //then
        assertEquals(
                List.of(
                        getLibraryEntry("libraries/example.jar"),
                        getLibraryEntry("libraries/example2.jar")
                ),
                result
        );
    }

    @Test
    void should_exclude_library_when_last_matching_rule_disallows_current_os() {
        //given
        List<RuntimeLibraryMetadata> libraries = List.of(
                getRuntimeLibraryMetadata(
                        "libraries/example.jar",
                        List.of(
                                new LibraryRule(LibraryRuleAction.ALLOW, OperatingSystem.WINDOWS),
                                new LibraryRule(LibraryRuleAction.DISALLOW, OperatingSystem.WINDOWS)
                        )
                )
        );

        //when
        List<LibraryEntry> result = selector.select(libraries, environment);

        //then
        assertTrue(result.isEmpty());
    }

    @Test
    void should_exclude_library_when_rules_exist_but_no_rule_matches_current_os() {
        //given
        List<RuntimeLibraryMetadata> libraries = List.of(
                getRuntimeLibraryMetadata(
                        "libraries/example.jar",
                        List.of(
                                new LibraryRule(LibraryRuleAction.ALLOW, OperatingSystem.LINUX)
                        )
                )
        );

        //when
        List<LibraryEntry> result = selector.select(libraries, environment);

        //then
        assertTrue(result.isEmpty());
    }

    @Test
    void should_reject_null_environment() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> selector.select(List.of(), null)
        );

        assertTrue(exception.getMessage().contains("environment"));
    }

    @Test
    void should_reject_null_value_in_list_of_libraries() {
        //given
        List<RuntimeLibraryMetadata> libraries = new ArrayList<>();
        libraries.add(null);

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> selector.select(libraries, environment)
        );
    }

    @Test
    void should_reject_null_list_of_libraries() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> selector.select(null, environment)
        );

        assertTrue(exception.getMessage().contains("libraries"));
    }

    @Test
    void should_return_empty_list_when_runtime_library_metadata_is_empty() {
        //given & when
        List<LibraryEntry> result = selector.select(List.of(), environment);

        //then
        assertEquals(
                List.of(),
                result
        );
    }

    @Test
    void should_select_runtime_libraries_as_library_entries() {
        //given
        RuntimeLibraryMetadata runtimeLibraryMetadata = getRuntimeLibraryMetadata("libraries/example.jar");

        //when
        List<LibraryEntry> result = selector.select(List.of(runtimeLibraryMetadata), environment);

        //then
        assertEquals(
                List.of(getLibraryEntry("libraries/example.jar")),
                result
        );
    }

    private RuntimeLibraryMetadata getRuntimeLibraryMetadata(String path) {
        return new RuntimeLibraryMetadata(
                new LibraryArtifactMetadata(
                        path,
                        "sha256",
                        100L,
                        "https://example.com/" + path
                ),
                List.of()
        );
    }

    private RuntimeLibraryMetadata getRuntimeLibraryMetadata(String path, List<LibraryRule> rules) {
        return getRuntimeLibraryMetadata(
                path,
                rules,
                Map.of(),
                Map.of()
        );
    }


    private RuntimeLibraryMetadata getRuntimeLibraryMetadata(
            String path,
            List<LibraryRule> rules,
            Map<String, LibraryArtifactMetadata> classifiers,
            Map<OperatingSystem, String> natives
    ) {
        return new RuntimeLibraryMetadata(
                new LibraryArtifactMetadata(
                        path,
                        "sha256",
                        100L,
                        "https://example.com/" + path
                ),
                rules,
                new LibraryClassifiersMetadata(classifiers),
                new LibraryNativesMetadata(natives)
        );
    }


    private LibraryEntry getLibraryEntry(String path) {
        return new LibraryEntry(
                path,
                "sha256",
                100L,
                "https://example.com/" + path
        );
    }

}
