package com.launcher.core.architecture.resource;

import com.launcher.core.resource.SafeResourcePathResolver;
import com.launcher.core.resource.UnsafeResourcePathException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SafeResourcePathResolverTest {
    private final SafeResourcePathResolver resolver = new SafeResourcePathResolver();
    private Path baseDirectory;

    @BeforeEach
    void setUp() {
        baseDirectory = Path.of("game");
    }

    @Test
    void should_reject_resource_path_that_escapes_base_directory() {
        //when & then
        UnsafeResourcePathException exception = assertThrows(
                UnsafeResourcePathException.class,
                () -> resolver.resolve(baseDirectory, "../test.jar")
        );

        assertTrue(
                exception.getMessage().contains("Resource path escapes base directory")
        );
    }

    @Test
    void should_reject_resource_path_that_has_absolute_path() {
        //given
        Path absolutePath = Path.of("/test.jar").toAbsolutePath();

        //when & then
        UnsafeResourcePathException exception = assertThrows(
                UnsafeResourcePathException.class,
                () -> resolver.resolve(baseDirectory, absolutePath.toString())
        );

        assertTrue(exception.getMessage().contains("Resource path must not be absolute"));
    }

    @Test
    void should_reject_resource_path_that_has_blank_path() {
        //when & then
        UnsafeResourcePathException exception = assertThrows(
                UnsafeResourcePathException.class,
                () -> resolver.resolve(baseDirectory, " ")
        );

        assertTrue(exception.getMessage().contains("Resource path must not be blank"));
    }

    @Test
    void should_reject_resource_path_that_has_null_value() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> resolver.resolve(baseDirectory, null)
        );

        assertTrue(exception.getMessage().contains("resourcePath"));
    }

    @Test
    void should_reject_base_directory_that_has_null_value() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> resolver.resolve(null, "test.jar")
        );

        assertTrue(exception.getMessage().contains("baseDirectory"));
    }

    @Test
    void should_allow_parent_segments_when_result_stays_inside_base_directory() {
        //when
        Path result = resolver.resolve(
                baseDirectory,
                "mods/../libraries/test.jar"
        );

        //then
        assertEquals(
                Path.of("game/libraries/test.jar"),
                result
        );
    }

}
