package com.launcher.core.architecture.game;

import com.launcher.core.game.GameLaunchPlan;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameLaunchPlanTest {

    @Test
    void should_create_immutable_command() {
        //given
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("TestMain");

        GameLaunchPlan gameLaunchPlan = new GameLaunchPlan(
                Path.of("game"),
                command
        );

        //when & then
        assertThrows(
                UnsupportedOperationException.class,
                () -> gameLaunchPlan.command().add("new-argument")
        );
    }

    @Test
    void should_reject_empty_command() {
        //when
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new GameLaunchPlan(
                        Path.of("game"),
                        Collections.emptyList()
                )
        );

        //then
        assertTrue(exception.getMessage().contains("command must not be empty"));
    }

    @Test
    void should_reject_nul_command() {
        //when
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new GameLaunchPlan(
                        Path.of("game"),
                        null
                )
        );

        //then
        assertTrue(exception.getMessage().contains("command"));
    }

    @Test
    void should_reject_null_game_directory() {

        //when
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new GameLaunchPlan(
                        null,
                        List.of("java", "TestMain")
                )
        );

        //then
        assertTrue(exception.getMessage().contains("gameDirectory"));
    }

}
