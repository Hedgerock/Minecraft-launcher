package com.launcher.storage.resolver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalWriteTargetResolverTest {
    private final LocalWriteTargetResolver resolver =
            new LocalWriteTargetResolver();

    @Test
    void should_reject_target_outside_trusted_root(
            @TempDir Path tempDir
    ) throws IOException {
        //given
        Path trustedTarget = tempDir.resolve("game");
        Files.createDirectories(trustedTarget);

        Path targetPath = trustedTarget
                .resolve("..")
                .resolve("outside")
                .resolve("library.jar");

        //when & then
        assertThrows(
                IOException.class,
                () -> resolver.prepareFileTarget(
                        trustedTarget,
                        targetPath
                )
        );
    }

    @Test
    void should_not_create_directory_outside_trusted_root_through_directory_redirect(
         @TempDir Path tempDir
    ) throws IOException {
        //given
        Path trustedTarget = tempDir.resolve("game");
        Path outsideDirectory = tempDir.resolve("outside");

        Files.createDirectories(trustedTarget);
        Files.createDirectories(outsideDirectory);

        Path directoryRedirect = trustedTarget.resolve("libraries");

        DirectoryRedirectFixture.create(
                directoryRedirect,
                outsideDirectory
        );

        Path targetPath = directoryRedirect
                .resolve("must-not-be-created")
                .resolve("library.jar");

        //when
        assertThrows(
                IOException.class,
                () -> resolver.prepareFileTarget(
                        trustedTarget,
                        targetPath
                )
        );

        //then
        assertFalse(
                Files.exists(
                        outsideDirectory.resolve(
                                "must-not-be-created"
                        )
                )
        );
    }

    @Test
    void should_allow_directory_redirect_resolving_inside_trusted_root(
            @TempDir Path tempDir
    ) throws IOException {
        //given
        Path trustedTarget = tempDir.resolve("game");
        Path actualDirectory = trustedTarget.resolve("storage");
        Path directoryRedirect = trustedTarget.resolve("libraries");

        Files.createDirectories(actualDirectory);
        DirectoryRedirectFixture.create(
                directoryRedirect,
                actualDirectory
        );

        Path targetPath = directoryRedirect
                .resolve("example")
                .resolve("library.jar");

        //when
        Path result = resolver.prepareFileTarget(
                trustedTarget,
                targetPath
        );

        //then
        assertEquals(
                targetPath.getParent().toRealPath(),
                result.getParent().toRealPath()
        );
        assertEquals(
                targetPath.getFileName(),
                result.getFileName()
        );

        assertTrue(
                Files.isDirectory(
                        actualDirectory.resolve("example")
                )
        );
    }

    @Test
    void should_prepare_file_target_when_trusted_root_does_not_exist(
         @TempDir Path tempDir
    ) throws IOException {
        //given
        Path trustedTarget = tempDir.resolve("game");
        Path targetPath = trustedTarget.resolve("client.jar");

        //when
        Path result = resolver.prepareFileTarget(
                trustedTarget,
                targetPath
        );

        //then
        assertEquals(
                targetPath.getParent().toRealPath(),
                result.getParent().toRealPath()
        );
        assertEquals(
                targetPath.getFileName(),
                result.getFileName()
        );

        assertTrue(Files.isDirectory(trustedTarget));
        assertFalse(Files.exists(targetPath));
    }

    @Test
    void should_prepare_nested_file_target(
            @TempDir Path tempDir
    ) throws IOException {
        //given
        Path trustedTarget = tempDir.resolve("game");
        Path targetPath = trustedTarget
                .resolve("libraries")
                .resolve("example")
                .resolve("library.jar");

        //when
        Path result = resolver.prepareFileTarget(
                trustedTarget,
                targetPath
        );

        //then
        assertEquals(
                targetPath.getParent().toRealPath(),
                result.getParent().toRealPath()
        );
        assertEquals(
                targetPath.getFileName(),
                result.getFileName()
        );
        assertTrue(
                Files.isDirectory(
                        trustedTarget
                                .resolve("libraries")
                                .resolve("example")
                )
        );
        assertFalse(Files.exists(targetPath));
    }

    @Test
    void should_reject_null_target_path(@TempDir Path tempDir) {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> resolver.prepareFileTarget(
                        tempDir.resolve("trustedRoot"),
                        null
                )
        );

        assertEquals(
                "targetPath",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_trusted_root(@TempDir Path tempDir) {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> resolver.prepareFileTarget(
                        null,
                        tempDir.resolve("targetPath")
                )
        );

        assertEquals(
                "trustedRoot",
                exception.getMessage()
        );
    }
}
