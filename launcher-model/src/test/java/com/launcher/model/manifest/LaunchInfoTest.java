package com.launcher.model.manifest;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LaunchInfoTest {

    @Test
    void should_reject_null_jvm_args() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LaunchInfo(
                        "MainClass",
                        null,
                        List.of("classpath", "arg1", "arg2")
                )
        );

        assertTrue(
                exception.getMessage().contains("jvmArgs")
        );
    }

    @Test
    void should_reject_null_game_args() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LaunchInfo(
                        "MainClass",
                        List.of("jvm", "arg1", "arg2"),
                        null
                )
        );

        assertTrue(
                exception.getMessage().contains("gameArgs")
        );
    }

    @Test
    void should_create_immutable_jvm_args() {
        //given
        List<String> jvmArgs = new ArrayList<>();

        jvmArgs.add("jvm");
        jvmArgs.add("arg1");
        jvmArgs.add("arg2");

        List<String> gameArgs = List.of("classpath", "arg1", "arg2");

        LaunchInfo launchInfo = new LaunchInfo("MainClass", jvmArgs, gameArgs);

        //when & then
        assertThrows(
                UnsupportedOperationException.class,
                () -> launchInfo.jvmArgs().add("new-argument")
        );

    }

    @Test
    void should_create_immutable_game_args() {
        //given
        List<String> gameArgs = new ArrayList<>();

        gameArgs.add("classpath");
        gameArgs.add("arg1");
        gameArgs.add("arg2");

        List<String> jvmArgs = List.of("jvm", "arg1", "arg2");

        LaunchInfo launchInfo = new LaunchInfo("MainClass", jvmArgs, gameArgs);

        //when & then
        assertThrows(
                UnsupportedOperationException.class,
                () -> launchInfo.gameArgs().add("new-argument")
        );
    }

    @Test
    void should_reject_null_jvm_arg() {
        //given
        List<String> jvmArgs = new ArrayList<>();

        jvmArgs.add(null);
        jvmArgs.add("arg1");
        jvmArgs.add("arg2");


        //when & then
        assertThrows(
                NullPointerException.class,
                () -> new LaunchInfo(
                        "MainClass",
                        jvmArgs,
                        List.of("classpath", "arg1", "arg2")
                )
        );

    }

    @Test
    void should_reject_null_game_arg() {
        //given
        List<String> gameArgs = new ArrayList<>();

        gameArgs.add(null);
        gameArgs.add("arg1");
        gameArgs.add("arg2");

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> new LaunchInfo(
                        "MainClass",
                        List.of("jvm", "arg1", "arg2"),
                        gameArgs
                )
        );
    }

    @Test
    void should_reject_null_main_class() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LaunchInfo(
                        null,
                        List.of("jvm", "arg1", "arg2"),
                        List.of("classpath", "arg1", "arg2")
                )
        );

        assertTrue(exception.getMessage().contains("mainClass"));
    }

    @Test
    void should_reject_blank_main_class() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LaunchInfo(
                        " ",
                        List.of("jvm", "arg1", "arg2"),
                        List.of("classpath", "arg1", "arg2")
                )
        );

        assertTrue(exception.getMessage()
                .contains("mainClass must not be blank"));
    }
}
