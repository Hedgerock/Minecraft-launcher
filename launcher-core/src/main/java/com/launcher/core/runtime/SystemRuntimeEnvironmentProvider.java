package com.launcher.core.runtime;

import com.launcher.model.runtime.OperatingSystem;
import com.launcher.model.runtime.RuntimeEnvironment;

import java.util.Objects;
import java.util.function.Supplier;

public final class SystemRuntimeEnvironmentProvider implements RuntimeEnvironmentProvider {
    private final Supplier<String> osNameSupplier;

    public SystemRuntimeEnvironmentProvider() {
        this(() -> System.getProperty("os.name"));
    }

    SystemRuntimeEnvironmentProvider(Supplier<String> osNameSupplier) {
        this.osNameSupplier = Objects.requireNonNull(osNameSupplier, "osNameSupplier");
    }

    @Override
    public RuntimeEnvironment current() {
        String osName = Objects.requireNonNull(osNameSupplier.get(), "osName");

        return new RuntimeEnvironment(
                resolveOperatingSystem(osName)
        );
    }

    private OperatingSystem resolveOperatingSystem(String osName) {
        String normalized = osName.toLowerCase();

        if (normalized.contains("win")) {
            return OperatingSystem.WINDOWS;
        }

        if (normalized.contains("mac")) {
            return OperatingSystem.MACOS;
        }

        boolean isLinux =
                normalized.contains("linux") ||
                normalized.contains("nux") ||
                normalized.contains("nix");

        if (isLinux) {
            return OperatingSystem.LINUX;
        }

        throw new IllegalStateException("Unsupported operating system: " + osName);
    }
}
