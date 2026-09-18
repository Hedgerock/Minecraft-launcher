package com.launcher.downloader.service;

import com.launcher.core.download.DownloadService;
import com.launcher.core.download.model.DownloadPlan;
import com.launcher.core.resource.ResourceSetConflictException;
import com.launcher.core.resource.ResourceSetPlanner;
import com.launcher.core.resource.SafeResourcePathResolver;
import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.downloader.exception.DownloadException;
import com.launcher.downloader.exception.DownloadExceptionReason;
import com.launcher.downloader.support.FixedDirectoryProvider;
import com.launcher.downloader.support.RecordingFileDownloader;
import com.launcher.downloader.support.RecordingResourcePathResolver;
import com.launcher.downloader.support.WritingFileDownloader;
import com.launcher.downloader.support.model.TestDownloadServiceResourcePathResolverRecord;
import com.launcher.model.manifest.ResourceEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultDownloadServiceTest {
    private ResourceSetPlanner resourceSetPlanner;
    private RecordingResourcePathResolver resourcePathResolver;

    @BeforeEach
    void setUp() {
        resourcePathResolver = new RecordingResourcePathResolver(Path.of("resolved/test-file.jar"));
        resourceSetPlanner = new ResourceSetPlanner(resourcePathResolver);
    }

    @Test
    void should_fail_when_paths_resolve_to_same_target_but_resources_are_incompatible(@TempDir Path tempDir) {
        //given
        Path gameDirectory = tempDir.resolve("game");
        RecordingFileDownloader downloader = new RecordingFileDownloader();

        ResourceSetPlanner resourceSetPlanner = new ResourceSetPlanner(
                new SafeResourcePathResolver()
        );

        DirectoryProvider fixedDirectoryProvider = new FixedDirectoryProvider(gameDirectory);

        DownloadService service = new DefaultDownloadService(
                fixedDirectoryProvider,
                downloader,
                resourceSetPlanner
        );

        ResourceEntry firstResource = getResourceEntry("mods/current-mode.jar", 100L, "http://file-entry.jar");
        ResourceEntry second = getResourceEntry("other/directory/current-mode.jar", 100L, "http://file-another-entry.jar");
        ResourceEntry conflictingResource = getResourceEntry("mods/./current-mode.jar", 100L, "https://file-entry.jar");

        List<ResourceEntry> resourceEntries = List.of(firstResource, second, conflictingResource);

        //when & then
        ResourceSetConflictException exception = assertThrows(
                ResourceSetConflictException.class,
                () -> service.download(new DownloadPlan(resourceEntries))
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

        assertTrue(downloader.getRequests().isEmpty());
    }

    @Test
    void should_merge_compatible_resources_when_paths_resolve_to_same_target(@TempDir Path tempDir) {
        //given
        Path gameDirectory = tempDir.resolve("game");
        RecordingFileDownloader downloader = new RecordingFileDownloader();

        ResourceSetPlanner resourceSetPlanner = new ResourceSetPlanner(
                new SafeResourcePathResolver()
        );

        DownloadService service = new DefaultDownloadService(
                new FixedDirectoryProvider(gameDirectory),
                downloader,
                resourceSetPlanner
        );

        ResourceEntry first = getResourceEntryWithSameSha256("mods/current-mode.jar", 100L, "http://file-entry.jar");
        ResourceEntry second = getResourceEntry("other/directory/current-mode.jar", 100L, "http://file-another-entry.jar");
        ResourceEntry third = getResourceEntryWithSameSha256("mods/./current-mode.jar", 100L, "http://file-entry.jar");

        List<ResourceEntry> resourceEntries = List.of(first, second, third);

        //when
        service.download(new DownloadPlan(resourceEntries));

        //then
        assertEquals(
                Stream.of(first, second).map((res) -> {
                            Path currentPath = resourcePathResolver.resolve(gameDirectory, res.path());
                            return new RecordingFileDownloader.DownloadRequest(res.url(), currentPath);
                        })
                        .toList(),
                downloader.getRequests()
        );
    }

    @Test
    void should_keep_first_resource_when_duplicates_are_compatible(@TempDir Path tempDir) {
        //given
        Path gameDirectory = tempDir.resolve("game");
        RecordingFileDownloader downloader = new RecordingFileDownloader();

        ResourceSetPlanner resourceSetPlanner = new ResourceSetPlanner(
                new SafeResourcePathResolver()
        );

        DownloadService service = new DefaultDownloadService(
                new FixedDirectoryProvider(gameDirectory),
                downloader,
                resourceSetPlanner
        );

        ResourceEntry first = getResourceEntry("mods/current-mode.jar", 100L, "http://file-entry.jar");
        ResourceEntry second = getResourceEntry("other/directory/current-mode.jar", 100L, "http://file-another-entry.jar");
        ResourceEntry third = getResourceEntry("mods/current-mode.jar", 100L, "http://file-entry.jar");

        List<ResourceEntry> resourceEntries = List.of(first, second, third);

        //when
        service.download(new DownloadPlan(resourceEntries));

        //then
        assertEquals(
                Stream.of(first, second).map((res) -> {
                    Path currentPath = resourcePathResolver.resolve(gameDirectory, res.path());
                    return new RecordingFileDownloader.DownloadRequest(res.url(), currentPath);
                })
                    .toList(),
                downloader.getRequests()
        );
    }


    @Test
    void should_pass_resolved_path_file_downloader(@TempDir Path tempDir) {
        //given
        Path gameDirectory = tempDir.resolve("game");
        Path resolvedPath = tempDir.resolve("resolved/test-file.jar");
        RecordingFileDownloader downloader = new RecordingFileDownloader();

        resourcePathResolver.setWithReturnResolvedPath();
        resourcePathResolver.setResolvedPath(resolvedPath);


        DownloadService service = new DefaultDownloadService(
                new FixedDirectoryProvider(gameDirectory),
                downloader,
                resourceSetPlanner
        );

        ResourceEntry resourceEntry = getResourceEntry("mods/current-mode.jar", 100L, "http://file-entry.jar");

        //when
        service.download(new DownloadPlan(List.of(resourceEntry)));

        //then
        assertEquals(resolvedPath, downloader.getRequests().getFirst().targetPath());
    }

    @Test
    void should_pass_game_directory_and_resource_path_to_resource_path_resolver(@TempDir Path tempDir) {
        //given
        Path gameDirectory = tempDir.resolve("game");
        RecordingFileDownloader downloader = new RecordingFileDownloader();

        DownloadService service = new DefaultDownloadService(
                new FixedDirectoryProvider(gameDirectory),
                downloader,
                resourceSetPlanner
        );

        ResourceEntry resourceEntry = getResourceEntry("mods/current-mode.jar", 100L, "http://file-entry.jar");

        DownloadPlan plan = new DownloadPlan(List.of(resourceEntry));
        List<TestDownloadServiceResourcePathResolverRecord> expectedCalls = Stream.of(resourceEntry)
                .map(resource ->
                        new TestDownloadServiceResourcePathResolverRecord(gameDirectory, resource.path()
                        )
                )
                .toList();

        //when
        service.download(plan);

        //then
        assertEquals(expectedCalls, resourcePathResolver.getResourcePathResolverRecords());

    }

    @Test
    void should_include_resource_path_and_target_path_when_file_downloader_failed() {
        //given
        Path gameDirectory = Path.of("/game");
        RecordingFileDownloader downloader = new RecordingFileDownloader(true);
        DownloadService service = new DefaultDownloadService(
                new FixedDirectoryProvider(gameDirectory),
                downloader,
                resourceSetPlanner
        );

        ResourceEntry resourceEntry = getResourceEntry("mods/current-mode.jar", 100L, "http://file-entry.jar");

        DownloadPlan plan = new DownloadPlan(List.of(resourceEntry));

        //when
        DownloadException exception = assertThrows(
                DownloadException.class,
                () -> service.download(plan)
        );

        //then
        assertEquals(DownloadExceptionReason.DOWNLOAD_FAILED, exception.getReason());
        assertEquals(resourceEntry.url(), exception.getUrl());
        assertEquals(resourceEntry.path(), exception.getPath().orElseThrow());
        assertEquals(gameDirectory.resolve("mods/current-mode.jar"), exception.getTargetPath().orElseThrow());
        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    void should_continue_when_downloaded_file_size_matches_manifest_entry(@TempDir Path tempDir) throws IOException {
        //given
        String content = "Hello test";

        ResourceEntry fileEntry = getResourceEntry(
                "mods/file.jar",
                content.getBytes(StandardCharsets.UTF_8).length,
                "http://file.jar"
        );

        DownloadPlan plan = new DownloadPlan(List.of(fileEntry));
        Path gameDirectory = tempDir.resolve("game");

        DownloadService service = new DefaultDownloadService(
                new FixedDirectoryProvider(gameDirectory),
                new WritingFileDownloader(content),
                resourceSetPlanner
        );
        //when
        service.download(plan);

        //then
        Path target = gameDirectory.resolve("mods/file.jar");

        assertTrue(Files.exists(target));
        assertEquals(content, Files.readString(target));
    }

    @Test
    void should_fail_when_downloaded_resource_size_can_not_written(@TempDir Path tempDir) {
        //given
        ResourceEntry resourceEntry = getResourceEntry("mods/file.jar", 12L, "http://file.jar");

        DownloadPlan plan = new DownloadPlan(List.of(resourceEntry));
        Path gameDirectory = tempDir.resolve("game");
        RecordingFileDownloader downloader = new RecordingFileDownloader();

        downloader.setCreateFile(false);

        DownloadService service = new DefaultDownloadService(
                new FixedDirectoryProvider(gameDirectory),
                downloader,
                resourceSetPlanner
        );

        //when
        DownloadException exception = assertThrows(
                DownloadException.class,
                () -> service.download(plan)
        );

        //then
        Path target = gameDirectory.resolve("mods/file.jar");

        assertFalse(Files.exists(target));

        assertEquals("http://file.jar", exception.getUrl());
        assertEquals(DownloadExceptionReason.SIZE_READ_FAILED, exception.getReason());
        assertTrue(exception.getMessage().contains("Failed to get resource size: mods/file.jar"));
        assertTrue(exception.getMessage().contains("mods/file.jar"));
        assertEquals(target, exception.getTargetPath().orElseThrow());
        assertEquals("mods/file.jar", exception.getPath().orElseThrow());
        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    void should_fail_when_downloaded_resource_size_does_not_match_manifest_entry(@TempDir Path tempDir)
            throws IOException {
        //given
        ResourceEntry resourceEntry = getResourceEntry("mods/file.jar", 12L, "http://file.jar");

        DownloadPlan plan = new DownloadPlan(List.of(resourceEntry));
        Path gameDirectory = tempDir.resolve("game");

        DownloadService service = new DefaultDownloadService(
                new FixedDirectoryProvider(gameDirectory),
                new WritingFileDownloader("Hello test"),
                resourceSetPlanner
        );

        //when

        DownloadException exception = assertThrows(
                DownloadException.class,
                () -> service.download(plan)
        );

        //then
        Path target = gameDirectory.resolve("mods/file.jar");

        assertTrue(Files.exists(target));
        assertEquals(10L, Files.size(target));
        assertTrue(exception.getMessage().contains("Downloaded resource size mismatch: mods/file.jar"));
        assertEquals(DownloadExceptionReason.SIZE_MISMATCH, exception.getReason());
        assertEquals(resourceEntry.url(), exception.getUrl());
        assertEquals(resourceEntry.path(), exception.getPath().orElseThrow());
        assertEquals(target, exception.getTargetPath().orElseThrow());

    }

    @Test
    void should_download_each_file_to_game_directory(@TempDir Path tempDir) {
        //given
        Path gameDirectory = tempDir.resolve("game");
        RecordingFileDownloader downloader = new RecordingFileDownloader();
        DownloadService service = new DefaultDownloadService(
                new FixedDirectoryProvider(gameDirectory),
                downloader,
                resourceSetPlanner
        );

        ResourceEntry firstFile = getResourceEntry("first.jar", 100L, "http://first.jar");
        ResourceEntry secondFile = getResourceEntry("second.jar", 100L, "http://second.jar");

        DownloadPlan plan = new DownloadPlan(List.of(firstFile, secondFile));

        //when
        service.download(plan);

        //then
        assertEquals(
                2,
                downloader.getRequests().size()
        );

    }

    @Test
    void should_create_target_path_from_game_directory_and_file_path(@TempDir Path tempDir) {
        //given
        Path gameDirectory = tempDir.resolve("game");
        RecordingFileDownloader downloader = new RecordingFileDownloader();
        DownloadService service = new DefaultDownloadService(
                new FixedDirectoryProvider(gameDirectory),
                downloader,
                resourceSetPlanner
        );

        ResourceEntry fileEntry = getResourceEntry("mods/current-mode.jar", 100L, "http://file-entry.jar");

        DownloadPlan plan = new DownloadPlan(List.of(fileEntry));

        //when
        service.download(plan);

        //then
        Path actualPath = gameDirectory.resolve("mods/current-mode.jar");

        assertEquals(
                actualPath,
                downloader.getRequests()
                        .getFirst()
                        .targetPath()
        );
    }

    @Test
    void should_propagate_downloader_failure() {
        //given
        Path gameDirectory = Path.of("/game");
        RecordingFileDownloader downloader = new RecordingFileDownloader(true);
        DownloadService service = new DefaultDownloadService(
                new FixedDirectoryProvider(gameDirectory),
                downloader,
                resourceSetPlanner
        );

        ResourceEntry fileEntry = getResourceEntry("mods/current-mode.jar", 100L, "http://file-entry.jar");

        DownloadPlan plan = new DownloadPlan(List.of(fileEntry));

        //then
        assertThrows(
                DownloadException.class,
                () -> service.download(plan)
        );

    }

    @Test
    void should_not_download_anything_when_download_plan_is_empty() {
        //given
        Path gameDirectory = Path.of("/game");
        RecordingFileDownloader downloader = new RecordingFileDownloader();
        DownloadService service = new DefaultDownloadService(
                new FixedDirectoryProvider(gameDirectory),
                downloader,
                resourceSetPlanner
        );

        DownloadPlan plan = new DownloadPlan(List.of());

        //when

        service.download(plan);

        //then
        assertTrue(downloader.getRequests().isEmpty());
    }

    private ResourceEntry getResourceEntry(String path, long size, String url) {
        return new ResourceEntry(
                path,
                "sha-" + path,
                size,
                url
        );
    }

    private ResourceEntry getResourceEntryWithSameSha256(String path, long size, String url) {
        return new ResourceEntry(
                path,
                "sha-256",
                size,
                url
        );
    }
}
