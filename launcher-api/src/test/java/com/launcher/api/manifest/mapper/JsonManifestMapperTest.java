package com.launcher.api.manifest.mapper;

import com.launcher.api.manifest.exception.ManifestMappingException;
import com.launcher.api.manifest.library.DefaultRuntimeLibrarySelector;
import com.launcher.api.manifest.library.RuntimeLibrarySelector;
import com.launcher.api.manifest.support.RecordingRuntimeLibrarySelector;
import com.launcher.model.manifest.FileEntry;
import com.launcher.model.manifest.LaunchInfo;
import com.launcher.model.manifest.LibraryArtifactMetadata;
import com.launcher.model.manifest.LibraryEntry;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.RuntimeLibraryMetadata;
import com.launcher.model.runtime.OperatingSystem;
import com.launcher.model.runtime.RuntimeEnvironment;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonManifestMapperTest {

    private JsonManifestMapper getMapper(RuntimeLibrarySelector runtimeLibrarySelector) {
        return new JsonManifestMapper(
                runtimeLibrarySelector,
                () -> new RuntimeEnvironment(OperatingSystem.WINDOWS)
        );
    }

    private JsonManifestMapper getMapper() {
        return getMapper(new DefaultRuntimeLibrarySelector());
    }

    @Test
    void should_pass_environment_and_runtime_libraries_metadata_to_selector() {
        //given
        RecordingRuntimeLibrarySelector selector = new RecordingRuntimeLibrarySelector();
        JsonManifestMapper mapper = getMapper(selector);
        String json = loadResource("manifest/test-valid-manifest.json");

        //when
        mapper.map(json);

        //then
        assertEquals(List.of(
                new RuntimeLibraryMetadata(
                        new LibraryArtifactMetadata(
                                "libraries/org/example/example.jar",
                                "library-sha256",
                                123456789L,
                                "https://localhost/files/libraries/org/example/example.jar"
                        )
                )
        ), selector.getLibraries());

        assertEquals(
                OperatingSystem.WINDOWS,
                selector.getEnvironment().operatingSystem()
        );
    }

    @Test
    void should_fail_when_required_manifest_field_is_missing() {
        //given
        JsonManifestMapper mapper = getMapper();
        String json = loadResource("manifest/test-some-empty-values-manifest.json");

        //then
        assertThrows(
                ManifestMappingException.class,
                () -> mapper.map(json)
        );
    }

    @Test
    void should_return_immutable_file_entries_from_manifest_json() {
        //given
        JsonManifestMapper mapper = getMapper();
        String json = loadResource("manifest/test-valid-manifest.json");
        FileEntry candidate = new FileEntry(
                "new-path",
                "new-sha-256",
                123L,
                "https://new-url.com/new-path"
        );

        //when
        Manifest manifest = mapper.map(json);

        //then
        assertThrows(
                UnsupportedOperationException.class,
                () -> manifest.files().add(candidate)
        );
    }

    @Test
    void should_throw_exception_when_manifest_is_invalid() {
        //given
        JsonManifestMapper mapper = getMapper();
        String json = loadResource("manifest/test-invalid-manifest.json");

        //when & then
        ManifestMappingException exception = assertThrows(
                ManifestMappingException.class,
                () -> mapper.map(json)
        );

        assertTrue(exception.getMessage().contains("Failed to parse manifest json"));

    }

    @Test
    void should_map_valid_manifest() {
        //given
        JsonManifestMapper mapper = getMapper();
        String json = loadResource("manifest/test-valid-manifest.json");

        //when
        Manifest manifest = mapper.map(json);

        //then
        assertNotNull(manifest);

        assertEquals("1.21.1", manifest.minecraftVersion());
        assertEquals("fabric", manifest.loader().type());
        assertEquals("0.16.10", manifest.loader().version());
        assertEquals(1, manifest.files().size());

        FileEntry fileEntry = manifest.files().getFirst();

        assertEquals("mods/example.jar", fileEntry.path());
        assertEquals("abc1234", fileEntry.sha256());
        assertEquals(123456789, fileEntry.size());
        assertEquals("https://localhost/files/mods/example.jar", fileEntry.url());

        LaunchInfo launchInfo = manifest.launchInfo();
        assertNotNull(launchInfo);

        assertEquals("net.minecraft.client.main.Main", launchInfo.mainClass());

        assertEquals(1, launchInfo.jvmArgs().size());
        assertEquals(
                List.of("-Xmx2G"),
                launchInfo.jvmArgs()
        );

        assertEquals(2, launchInfo.gameArgs().size());
        assertEquals(
                List.of("--username", "Player"),
                launchInfo.gameArgs()
        );

        assertEquals(2, launchInfo.classpath().size());
        assertEquals(
                List.of("libraries/example.jar", "client.jar"),
                launchInfo.classpath()
        );

        assertEquals("java", launchInfo.javaExecutable());

        assertEquals(1, manifest.libraries().size());

        LibraryEntry libraryEntry = manifest.libraries().getFirst();

        assertEquals(
                "libraries/org/example/example.jar",
                libraryEntry.path()
        );
        assertEquals(
                "https://localhost/files/libraries/org/example/example.jar",
                libraryEntry.url()
        );
        assertEquals(
                "library-sha256",
                libraryEntry.sha256()
        );
        assertEquals(
                123456789L,
                libraryEntry.size()
        );
    }

    private String loadResource(String name) {
        try (InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream(name)
        ) {
            if (input == null) {
                throw new IllegalArgumentException("Resource not found: " + name);
            }

            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
