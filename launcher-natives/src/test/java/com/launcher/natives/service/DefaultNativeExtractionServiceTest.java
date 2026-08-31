package com.launcher.natives.service;

import com.launcher.core.natives.model.NativeExtractionPlan;
import com.launcher.model.manifest.LibraryEntry;
import com.launcher.natives.exception.NativeExtractionException;
import com.launcher.natives.support.FixedDirectoryProvider;
import com.launcher.natives.support.RecordingResourcePathResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultNativeExtractionServiceTest {
    private FixedDirectoryProvider directoryProvider;
    private RecordingResourcePathResolver resourcePathResolver;
    private DefaultNativeExtractionService service;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        directoryProvider = new FixedDirectoryProvider(tempDir);
        resourcePathResolver = new RecordingResourcePathResolver();
        service = new DefaultNativeExtractionService(
                directoryProvider,
                resourcePathResolver
        );
    }

    @Test
    void should_resolve_artifact_path_against_game_directory() throws IOException {
        //given
        Path gameDirectory = directoryProvider.directories().game();
        Path firstPath = gameDirectory.resolve("libraries/first.jar");
        Path secondPath = gameDirectory.resolve("libraries/second.jar");
        Path targetDirectory = directoryProvider.directories().natives();

        createArchive(
                firstPath,
                "first.dll",
                "first-content"
        );

        createArchive(
                secondPath,
                "second.dll",
                "second-content"
        );

        LibraryEntry artifact = getArtifact("libraries/first");
        LibraryEntry secondArtifact = getArtifact("libraries/second");

        NativeExtractionPlan extractionPlan = new NativeExtractionPlan(
                List.of(artifact, secondArtifact),
                targetDirectory
        );

        //when
        service.extract(extractionPlan);

        //then
        assertTrue(
                resourcePathResolver.getBaseDirectories().stream()
                        .allMatch(path -> path.equals(gameDirectory))
        );

        assertTrue(
                resourcePathResolver.getResourcePaths().stream()
                        .allMatch(path -> path.startsWith("libraries/"))
        );
    }

    @Test
    void should_reject_null_resource_path_resolver() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new DefaultNativeExtractionService(
                        directoryProvider,
                        null
                )
        );

        assertTrue(exception.getMessage().contains("resourcePathResolver"));
    }

    @Test
    void should_reject_null_directory_provider() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new DefaultNativeExtractionService(
                        null,
                        resourcePathResolver
                )
        );

        assertTrue(exception.getMessage().contains("directoryProvider"));
    }

    @Test
    void should_reject_null_plan() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> service.extract(null)
        );

        assertTrue(exception.getMessage().contains("plan"));
    }

    @Test
    void should_not_extract_anything_when_plan_is_empty() {
        //given
        Path targetDirectory = directoryProvider.directories().natives();

        NativeExtractionPlan extractionPlan = new NativeExtractionPlan(
                List.of(),
                targetDirectory
        );

        //when
        service.extract(extractionPlan);

        //then
        assertTrue(
                resourcePathResolver.getBaseDirectories().isEmpty()
        );
    }

    @Test
    void should_fail_when_source_artifact_does_not_exist() {
        //given
        Path targetDirectory = directoryProvider.directories().natives();
        LibraryEntry firstArtifact = getArtifact("libraries/another-value");

        //when
        NativeExtractionPlan extractionPlan = new NativeExtractionPlan(
                List.of(firstArtifact),
                targetDirectory
        );

        //when
        NativeExtractionException exception = assertThrows(
                NativeExtractionException.class,
                () -> service.extract(extractionPlan)
        );

        //then
        assertEquals(
                "Failed to extract native artifact",
                exception.getMessage()
        );

        assertInstanceOf(
                NoSuchFileException.class,
                exception.getCause()
        );
    }

    @Test
    void should_fail_when_archive_entry_escapes_target_directory() throws IOException {
        //given
        Path gameDirectory = directoryProvider.directories().game();
        Path archivePath = gameDirectory.resolve("libraries/outside.jar");
        Path targetDirectory = directoryProvider.directories().natives();

        createArchive(
                archivePath,
                "../outside.dll",
                "content"
        );

        LibraryEntry firstArtifact = getArtifact("libraries/outside");

        //when
        NativeExtractionPlan extractionPlan = new NativeExtractionPlan(
                List.of(firstArtifact),
                targetDirectory
        );

        //when
        NativeExtractionException exception = assertThrows(
                NativeExtractionException.class,
                () -> service.extract(extractionPlan)
        );

        //then
        assertEquals(
                "Native archive entry escapes target directory",
                exception.getMessage()
        );

        assertFalse(
                Files.exists(targetDirectory.getParent().resolve("outside.dll"))
        );
    }

    @Test
    void should_extract_each_native_artifact() throws IOException {
        //given
        Path gameDirectory = directoryProvider.directories().game();
        Path targetDirectory = directoryProvider.directories().natives();

        Path firstArchive = gameDirectory.resolve("libraries/first-native.jar");
        Path secondArchive = gameDirectory.resolve("libraries/second-native.jar");

        createArchive(firstArchive, "first-native.dll", "first-native-content");
        createArchive(secondArchive, "second-native.dll", "second-native-content");

        LibraryEntry firstArtifact = getArtifact("libraries/first-native");
        LibraryEntry secondArtifact = getArtifact("libraries/second-native");

        NativeExtractionPlan extractionPlan = new NativeExtractionPlan(
                List.of(firstArtifact, secondArtifact),
                targetDirectory
        );

        //when
        service.extract(extractionPlan);

        //then
        assertTrue(
                Files.exists(targetDirectory.resolve("first-native.dll"))
        );

        assertTrue(
                Files.exists(targetDirectory.resolve("second-native.dll"))
        );

        assertEquals(
                "first-native-content",
                Files.readString(targetDirectory.resolve("first-native.dll"))
        );

        assertEquals(
                "second-native-content",
                Files.readString(targetDirectory.resolve("second-native.dll"))
        );
    }

    @Test
    void should_extract_file_from_native_artifact_to_target_directory()
            throws IOException {
        //given
        Path gameDirectory = directoryProvider.directories().game();
        Path archivePath = gameDirectory.resolve("libraries/natives.jar");
        Path targetDirectory = directoryProvider.directories().natives();

        createArchive(
                archivePath,
                "native.dll",
                "native-content"
        );

        LibraryEntry artifact = getArtifact("libraries/natives");

        NativeExtractionPlan extractionPlan = new NativeExtractionPlan(
                List.of(artifact),
                targetDirectory
        );

        //when
        service.extract(extractionPlan);

        //then
        Path extractedFile = targetDirectory.resolve("native.dll");
        assertTrue(Files.exists(extractedFile));
        assertEquals(
                "native-content",
                Files.readString(extractedFile)
        );
    }

    @Test
    void should_create_nested_directories_when_archive_entry_is_nested() throws IOException {
        //given
        Path gameDirectory = directoryProvider.directories().game();
        Path archivePath = gameDirectory.resolve("libraries/natives.jar");
        Path targetDirectory = directoryProvider.directories().natives();

        createArchive(
                archivePath,
                "nested/dir/native.dll",
                "native-content"
        );

        LibraryEntry artifact = getArtifact("libraries/natives");

        NativeExtractionPlan extractionPlan = new NativeExtractionPlan(
                List.of(artifact),
                targetDirectory
        );

        //when
        service.extract(extractionPlan);

        //then
        Path nestedDirectory =
                targetDirectory.resolve("nested/dir");

        Path extractedFile = nestedDirectory.resolve("native.dll");

        assertTrue(Files.isDirectory(nestedDirectory));

        assertEquals(
                "native-content",
                Files.readString(extractedFile)
        );
    }

    private void createArchive(
            Path archivePath,
            String entryName,
            String content
    ) throws IOException {
        Files.createDirectories(archivePath.getParent());

        try (
                OutputStream outputStream = Files.newOutputStream(archivePath);
                ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)
        ) {
            zipOutputStream.putNextEntry(
                    new ZipEntry(entryName)
            );

            zipOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
            zipOutputStream.closeEntry();
        }
    }

    private LibraryEntry getArtifact(String path) {
        return new LibraryEntry(
                path + ".jar",
                "sha256-" + path,
                123L,
                "https://test-url.com/" + path
        );
    }
}
