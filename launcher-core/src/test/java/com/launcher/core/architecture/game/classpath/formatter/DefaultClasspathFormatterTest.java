package com.launcher.core.architecture.game.classpath.formatter;

import com.launcher.core.game.classpath.GameClasspath;
import com.launcher.core.game.classpath.formatter.DefaultClasspathFormatter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultClasspathFormatterTest {
    private final DefaultClasspathFormatter formatter = new DefaultClasspathFormatter();

    @Test
    void should_reject_null_classpath() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> formatter.format(null)
        );

        assertTrue(exception.getMessage().contains("classpath"));
    }

    @Test
    void should_format_classpath_platform_path_separator() {
        //given
        Path firstEntry = Path.of("game/libraries/example.jar");
        Path secondEntry = Path.of("game/client.jar");

        GameClasspath gameClasspath = new GameClasspath(
                List.of(firstEntry, secondEntry)
        );

        //when
        String formattedClasspath = formatter.format(gameClasspath);

        //then
        assertEquals(
                String.join(
                        File.pathSeparator,
                        firstEntry.toString(),
                        secondEntry.toString()
                ),
                formattedClasspath
        );
    }

}
