package com.launcher.model.manifest;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LaunchInfoTest {

    @Test
    void should_create_launch_info_with_java_executable() {
        //given & when
        LaunchInfo launchInfo = new LaunchInfo(
                getDefaultMainClass(),
                getDefaultJvmArgs(),
                getDefaultGameArgs(),
                getDefaultClasspath(),
                getDefaultJavaExecutable()
        );

        //then
        assertEquals("java", launchInfo.javaExecutable());
    }

    @Test
    void should_reject_blank_java_executable() {
        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LaunchInfo(
                        getDefaultMainClass(),
                        getDefaultJvmArgs(),
                        getDefaultGameArgs(),
                        getDefaultClasspath(),
                        " "
                )
        );

        assertTrue(exception.getMessage().contains("javaExecutable"));
    }

    @Test
    void should_reject_null_java_executable() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LaunchInfo(
                        getDefaultMainClass(),
                        getDefaultJvmArgs(),
                        getDefaultGameArgs(),
                        getDefaultClasspath(),
                        null
                )
        );

        assertTrue(exception.getMessage().contains("javaExecutable"));
    }

    @Test
    void should_prevent_external_mutation_for_game_args() {
        //given
        String firstEntry = "classpath";
        String secondEntry = "arg1";

        List<String> gameArgs = new ArrayList<>();

        gameArgs.add(firstEntry);
        gameArgs.add(secondEntry);

        LaunchInfo launchInfo = new LaunchInfo(
                getDefaultMainClass(),
                getDefaultJvmArgs(),
                gameArgs,
                getDefaultClasspath(),
                getDefaultJavaExecutable()
        );

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

        List<String> jvmArgs = new ArrayList<>();

        jvmArgs.add(firstEntry);
        jvmArgs.add(secondEntry);

        LaunchInfo launchInfo = new LaunchInfo(
                getDefaultMainClass(),
                jvmArgs,
                getDefaultGameArgs(),
                getDefaultClasspath(),
                getDefaultJavaExecutable()
        );

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

        List<String> classpath = new ArrayList<>();

        classpath.add(firstEntry);
        classpath.add(secondEntry);

        LaunchInfo launchInfo = new LaunchInfo(
                getDefaultMainClass(),
                getDefaultJvmArgs(),
                getDefaultGameArgs(),
                classpath,
                getDefaultJavaExecutable()
        );

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

        List<String> classpath = new ArrayList<>();

        classpath.add(firstEntry);
        classpath.add(secondEntry);

        LaunchInfo launchInfo = new LaunchInfo(
                getDefaultMainClass(),
                getDefaultJvmArgs(),
                getDefaultGameArgs(),
                classpath,
                getDefaultJavaExecutable()
        );

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
        List<String> classPathWithNullValue = new ArrayList<>();

        classPathWithNullValue.add("libraries/example.jar");
        classPathWithNullValue.add(null);

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> new LaunchInfo(
                        getDefaultMainClass(),
                        getDefaultJvmArgs(),
                        getDefaultGameArgs(),
                        classPathWithNullValue,
                        getDefaultJavaExecutable()
                )
        );
    }

    @Test
    void should_reject_empty_classpath() {
        //given
        List<String> emptyClasspath = Collections.emptyList();

        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LaunchInfo(
                        getDefaultMainClass(),
                        getDefaultJvmArgs(),
                        getDefaultGameArgs(),
                        emptyClasspath,
                        getDefaultJavaExecutable()
                )
        );

        assertTrue(
                exception.getMessage().contains("classpath must not be empty")
        );
    }

    @Test
    void should_reject_null_classpath() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LaunchInfo(
                        getDefaultMainClass(),
                        getDefaultJvmArgs(),
                        getDefaultGameArgs(),
                        null,
                        getDefaultJavaExecutable()
                )
        );

        assertTrue(
                exception.getMessage().contains("classpath")
        );
    }

    @Test
    void should_reject_null_jvm_args() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LaunchInfo(
                        getDefaultMainClass(),
                        null,
                        getDefaultGameArgs(),
                        getDefaultClasspath(),
                        getDefaultJavaExecutable()
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
                        getDefaultMainClass(),
                        getDefaultJvmArgs(),
                        null,
                        getDefaultClasspath(),
                        getDefaultJavaExecutable()
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

        LaunchInfo launchInfo = new LaunchInfo(
                getDefaultMainClass(),
                jvmArgs,
                getDefaultGameArgs(),
                getDefaultClasspath(),
                getDefaultJavaExecutable()
        );

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

        LaunchInfo launchInfo = new LaunchInfo(
                getDefaultMainClass(),
                getDefaultJvmArgs(),
                gameArgs,
                getDefaultClasspath(),
                getDefaultJavaExecutable()
        );

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
                        getDefaultMainClass(),
                        jvmArgs,
                        getDefaultGameArgs(),
                        getDefaultClasspath(),
                        getDefaultJavaExecutable()
                )
        );

    }

    @Test
    void should_reject_null_game_arg() {
        //given
        List<String> gameArgs = new ArrayList<>();

        gameArgs.add(null);
        gameArgs.add("Player");
        gameArgs.add("--userRole");

        //when & then
        assertThrows(
                NullPointerException.class,
                () -> new LaunchInfo(
                        getDefaultMainClass(),
                        getDefaultJvmArgs(),
                        gameArgs,
                        getDefaultClasspath(),
                        getDefaultJavaExecutable()
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
                        getDefaultJvmArgs(),
                        getDefaultGameArgs(),
                        getDefaultClasspath(),
                        getDefaultJavaExecutable()
                )
        );

        assertTrue(exception.getMessage().contains("mainClass"));
    }

    @Test
    void should_reject_blank_main_class() {
        String blankMainClass = " ";

        //when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LaunchInfo(
                        blankMainClass,
                        getDefaultJvmArgs(),
                        getDefaultGameArgs(),
                        getDefaultClasspath(),
                        getDefaultJavaExecutable()
                )
        );

        assertTrue(exception.getMessage()
                .contains("mainClass must not be blank"));
    }

    private String getDefaultJavaExecutable() {
        return "java";
    }

    private String getDefaultMainClass() {
        return "MainClass";
    }

    private List<String> getDefaultJvmArgs() {
        return List.of("jvm", "arg1", "arg2");
    }

    private List<String> getDefaultGameArgs() {
        return List.of("--username", "Player", "--userRole");
    }

    private List<String> getDefaultClasspath() {
        return List.of("libraries/example.jar", "client.jar");
    }
}
