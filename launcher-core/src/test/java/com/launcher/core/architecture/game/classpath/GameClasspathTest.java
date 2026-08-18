package com.launcher.core.architecture.game.classpath;

import com.launcher.core.game.classpath.GameClasspath;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameClasspathTest {

    @Test
    void should_return_immutable_entries() {
        //given
        Path firstEntry = Path.of("entry1");
        Path secondEntry = Path.of("entry2");

        List<Path> entries = new ArrayList<>();

        entries.add(firstEntry);
        entries.add(secondEntry);

        GameClasspath gameClassPath = new GameClasspath(entries);

        //when & then
        assertThrows(
                UnsupportedOperationException.class,
                () -> gameClassPath.entries().add(Path.of("entry3"))
        );
    }

    @Test
    void should_protect_entries_from_external_mutation() {
        //given
        Path firstEntry = Path.of("entry1");
        Path secondEntry = Path.of("entry2");

        List<Path> entries = new ArrayList<>();

        entries.add(firstEntry);
        entries.add(secondEntry);

        GameClasspath gameClassPath = new GameClasspath(entries);

        //when
        entries.add(Path.of("entry3"));

        //then
        assertEquals(
                List.of(firstEntry, secondEntry),
                gameClassPath.entries()
        );
    }

    @Test
    void should_reject_null_entry() {
        //given
        List<Path> entries = new ArrayList<>();

        entries.add(Path.of("entry1"));
        entries.add(null);

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> new GameClasspath(entries)
        );
    }

    @Test
    void should_reject_empty_entries() {
        //given
        List<Path> entries = Collections.emptyList();

        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new GameClasspath(entries)
        );

        assertTrue(exception.getMessage().contains("entries must not be empty"));
    }

    @Test
    void should_reject_null_entries() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new GameClasspath(null)
        );

        assertTrue(exception.getMessage().contains("entries"));

    }

    @Test
    void should_create_game_classpath_from_entries() {
        //given
        List<Path> entries = List.of(
                Path.of("entry1"),
                Path.of("entry2")
        );

        //when
        GameClasspath gameClassPath = new GameClasspath(entries);

        //then
        assertEquals(entries, gameClassPath.entries());

    }

}
