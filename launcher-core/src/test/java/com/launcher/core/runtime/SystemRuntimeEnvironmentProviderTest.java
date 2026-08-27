package com.launcher.core.runtime;

import com.launcher.model.runtime.OperatingSystem;
import com.launcher.model.runtime.RuntimeEnvironment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemRuntimeEnvironmentProviderTest {

    @Test
    void should_reject_null_os_name_supplier() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new SystemRuntimeEnvironmentProvider(null)
        );

        assertTrue(exception.getMessage().contains("osNameSupplier"));
    }

    @Test
    void should_throw_when_operating_system_is_undefined() {
        RuntimeEnvironmentProvider provider =
                new SystemRuntimeEnvironmentProvider(() -> "Solaris");

        //when & then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                provider::current
        );

        assertTrue(exception.getMessage().contains("Unsupported operating system: Solaris"));
    }

    @Test
    void should_resolve_macos_runtime_environment() {
        //given
        RuntimeEnvironmentProvider provider = new SystemRuntimeEnvironmentProvider(
                () -> "Mac OS X"
        );

        //when
        RuntimeEnvironment runtimeEnvironment = provider.current();

        //then
        assertEquals(OperatingSystem.MACOS, runtimeEnvironment.operatingSystem());
    }

    @Test
    void should_resolve_linux_runtime_environment() {
        //given
        RuntimeEnvironmentProvider provider = new SystemRuntimeEnvironmentProvider(
                () -> "Linux"
        );

        //when
        RuntimeEnvironment runtimeEnvironment = provider.current();

        //then
        assertEquals(OperatingSystem.LINUX, runtimeEnvironment.operatingSystem());
    }

    @Test
    void should_resolve_windows_runtime_environment() {
        //given
        RuntimeEnvironmentProvider provider = new SystemRuntimeEnvironmentProvider(
                () -> "Windows 11"
        );

        //when
        RuntimeEnvironment runtimeEnvironment = provider.current();

        //then
        assertEquals(OperatingSystem.WINDOWS, runtimeEnvironment.operatingSystem());
    }

}
