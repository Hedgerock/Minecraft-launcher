package com.launcher.core.runtime;

import com.launcher.model.manifest.LaunchInfo;
import com.launcher.model.runtime.JavaExecutableReference;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManifestJavaRuntimeSelectorTest {
    private final ManifestJavaRuntimeSelector selector = new ManifestJavaRuntimeSelector();

    @Test
    void should_select_java_executable_from_launch_info() {
        //given
        LaunchInfo launchInfo = new LaunchInfo(
                "MainClass",
                List.of(),
                List.of(),
                List.of("test-classpath.jar"),
                "java-custom"
        );

        //when
        JavaExecutableReference result = selector.selectJavaExecutable(launchInfo);

        //then
        assertTrue(result.isCommandName());

        assertEquals(
                "java-custom",
                result.value()
        );
    }

    @Test
    void should_reject_null_launch_info() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> selector.selectJavaExecutable(null)
        );

        assertTrue(exception.getMessage().contains("launchInfo"));
    }

}
