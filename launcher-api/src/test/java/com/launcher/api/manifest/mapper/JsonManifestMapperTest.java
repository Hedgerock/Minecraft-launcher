package com.launcher.api.manifest.mapper;

import com.launcher.api.manifest.exception.ManifestMappingException;
import com.launcher.model.manifest.FileEntry;
import com.launcher.model.manifest.LaunchInfo;
import com.launcher.model.manifest.Manifest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonManifestMapperTest {
    private final JsonManifestMapper mapper = new JsonManifestMapper();

    @Test
    void should_fail_when_required_manifest_field_is_missing() {
        //given
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
        assertEquals(
                "libraries/org/example/example.jar",
                manifest.libraries().getFirst().path()
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
