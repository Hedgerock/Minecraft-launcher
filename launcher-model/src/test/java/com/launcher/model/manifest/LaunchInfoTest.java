package com.launcher.model.manifest;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LaunchInfoTest {

    @Test
    void should_prevent_external_mutation_for_game_args() {
        //given
        String firstEntry = "classpath";
        String secondEntry = "arg1";

        String mainClass = "MainClass";
        List<String> jvmArgs = List.of("jvm", "arg1", "arg2");
        List<String> gameArgs = new ArrayList<>();
        List<String> classpath = List.of("libraries/example.jar", "client.jar");

        gameArgs.add(firstEntry);
        gameArgs.add(secondEntry);

        LaunchInfo launchInfo = new LaunchInfo(mainClass, jvmArgs, gameArgs, classpath);

        //when
        gameArgs.add("arg2");

        //then
        assertEquals(
                2,
                launchInfo.gameArgs().size()
        );

        assertEquals(
                List.of(firstEntry, secondEntry),
                launchInfo.gameArgs()
        );
    }

    @Test
    void should_prevent_external_mutation_for_jvm_args() {
        //given
        String firstEntry = "jvm";
        String secondEntry = "arg1";

        String mainClass = "MainClass";
        List<String> jvmArgs = new ArrayList<>();
        List<String> gameArgs = List.of("--username", "Player", "--userRole");
        List<String> classpath = List.of("libraries/example.jar", "client.jar");

        jvmArgs.add(firstEntry);
        jvmArgs.add(secondEntry);

        LaunchInfo launchInfo = new LaunchInfo(mainClass, jvmArgs, gameArgs, classpath);

        //when
        jvmArgs.add("admin");

        //then
        assertEquals(
                2,
                launchInfo.jvmArgs().size()
        );

        assertEquals(
                List.of(firstEntry, secondEntry),
                launchInfo.jvmArgs()
        );
    }

    @Test
    void should_create_immutable_classpath() {
        //given
        String firstEntry = "libraries/example.jar";
        String secondEntry = "client.jar";

        String mainClass = "MainClass";
        List<String> jvmArgs = List.of("jvm", "arg1", "arg2");
        List<String> gameArgs = List.of("--username", "Player", "--userRole");

        List<String> classpath = new ArrayList<>();

        classpath.add(firstEntry);
        classpath.add(secondEntry);

        LaunchInfo launchInfo = new LaunchInfo(mainClass, jvmArgs, gameArgs, classpath);

        //when & then
        assertThrows(
                UnsupportedOperationException.class,
                () -> launchInfo.classpath().add(
                        "libraries/example2.jar"
                )
        );

    }

    @Test
    void should_prevent_external_mutation_for_classpath() {
        //given
        String firstEntry = "libraries/example.jar";
        String secondEntry = "client.jar";

        String mainClass = "MainClass";
        List<String> jvmArgs = List.of("jvm", "arg1", "arg2");
        List<String> gameArgs = List.of("--username", "Player", "--userRole");

        List<String> classpath = new ArrayList<>();

        classpath.add(firstEntry);
        classpath.add(secondEntry);

        LaunchInfo launchInfo = new LaunchInfo(mainClass, jvmArgs, gameArgs, classpath);

        //when
        classpath.add("libraries/example2.jar");

        //then
        assertEquals(
                2,
                launchInfo.classpath().size()
        );

        assertEquals(
                List.of(firstEntry, secondEntry),
                launchInfo.classpath()
        );
    }

    @Test
    void should_reject_null_value_in_classpath() {
        //given
        String mainClass = "MainClass";
        List<String> jvmArgs = List.of("jvm", "arg1", "arg2");
        List<String> gameArgs = List.of("--username", "Player", "--userRole");
        List<String> classPathWithNullValue = new ArrayList<>();

        classPathWithNullValue.add("libraries/example.jar");
        classPathWithNullValue.add(null);

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> new LaunchInfo(
                        mainClass,
                        jvmArgs,
                        gameArgs,
                        classPathWithNullValue
                )
        );
    }

    @Test
    void should_reject_empty_classpath() {
        //given
        String mainClass = "MainClass";
        List<String> jvmArgs = List.of("jvm", "arg1", "arg2");
        List<String> gameArgs = List.of("--username", "Player", "--userRole");
        List<String> emptyClasspath = Collections.emptyList();

        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LaunchInfo(
                        mainClass,
                        jvmArgs,
                        gameArgs,
                        emptyClasspath
                )
        );

        assertTrue(
                exception.getMessage().contains("classpath must not be empty")
        );
    }

    @Test
    void should_reject_null_classpath() {
        //given
        String mainClass = "MainClass";
        List<String> jvmArgs = List.of("jvm", "arg1", "arg2");
        List<String> gameArgs = List.of("--username", "Player", "--userRole");

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LaunchInfo(
                        mainClass,
                        jvmArgs,
                        gameArgs,
                        null
                )
        );

        assertTrue(
                exception.getMessage().contains("classpath")
        );
    }

    @Test
    void should_reject_null_jvm_args() {
        //given
        String mainClass = "MainClass";
        List<String> gameArgs = List.of("--username", "Player", "--userRole");
        List<String> classpath = List.of("libraries/example.jar", "client.jar");

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LaunchInfo(
                        mainClass,
                        null,
                        gameArgs,
                        classpath
                )
        );

        assertTrue(
                exception.getMessage().contains("jvmArgs")
        );
    }

    @Test
    void should_reject_null_game_args() {
        //given
        String mainClass = "MainClass";
        List<String> jvmArgs = List.of("jvm", "arg1", "arg2");
        List<String> classpath = List.of("libraries/example.jar", "client.jar");

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LaunchInfo(
                        mainClass,
                        jvmArgs,
                        null,
                        classpath
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

        List<String> gameArgs = List.of("--username", "Player", "--userRole");
        List<String> classpath = List.of("libraries/example.jar", "client.jar");

        LaunchInfo launchInfo = new LaunchInfo("MainClass", jvmArgs, gameArgs, classpath);

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

        gameArgs.add("--username");
        gameArgs.add("Player");
        gameArgs.add("--userRole");

        List<String> jvmArgs = List.of("jvm", "arg1", "arg2");
        List<String> classpath = List.of("libraries/example.jar", "client.jar");

        LaunchInfo launchInfo = new LaunchInfo("MainClass", jvmArgs, gameArgs, classpath);

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

        List<String> gameArgs = List.of("--username", "Player", "--userRole");
        List<String> classpath = List.of("libraries/example.jar", "client.jar");

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> new LaunchInfo(
                        "MainClass",
                        jvmArgs,
                        gameArgs,
                        classpath
                )
        );

    }

    @Test
    void should_reject_null_game_arg() {
        //given
        String mainClass = "MainClass";
        List<String> gameArgs = new ArrayList<>();
        List<String> jvmArgs = List.of("jvm", "arg1", "arg2");

        gameArgs.add(null);
        gameArgs.add("Player");
        gameArgs.add("--userRole");

        List<String> classpath = List.of("libraries/example.jar", "client.jar");

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> new LaunchInfo(
                        mainClass,
                        jvmArgs,
                        gameArgs,
                        classpath
                )
        );
    }

    @Test
    void should_reject_null_main_class() {
        //given
        List<String> jvmArgs = List.of("jvm", "arg1", "arg2");
        List<String> gameArgs = List.of("--username", "User", "--userRole");
        List<String> classpath = List.of("libraries/example.jar", "client.jar");

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LaunchInfo(
                        null,
                        jvmArgs,
                        gameArgs,
                        classpath
                )
        );

        assertTrue(exception.getMessage().contains("mainClass"));
    }

    @Test
    void should_reject_blank_main_class() {
        String blankMainClass = " ";
        List<String> jvmArgs = List.of("jvm", "arg1", "arg2");
        List<String> gameArgs = List.of("--username", "User", "--userRole");
        List<String> classpath = List.of("libraries/example.jar", "client.jar");

        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LaunchInfo(
                        blankMainClass,
                        jvmArgs,
                        gameArgs,
                        classpath
                )
        );

        assertTrue(exception.getMessage()
                .contains("mainClass must not be blank"));
    }
}
