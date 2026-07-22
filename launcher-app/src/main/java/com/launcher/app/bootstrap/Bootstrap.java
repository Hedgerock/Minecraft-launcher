package com.launcher.app.bootstrap;

import com.launcher.core.LauncherEngine;
import com.launcher.core.assembly.ApplicationAssembly;
import com.launcher.core.assembly.DefaultApplicationAssembly;
import com.launcher.core.configuration.LauncherConfiguration;

public class Bootstrap {
    private final LauncherConfiguration launcherConfiguration;

    public Bootstrap(LauncherConfiguration launcherConfiguration) {
        this.launcherConfiguration = launcherConfiguration;
    }

    public LauncherEngine createEngine() {
        ApplicationAssembly applicationAssembly = new DefaultApplicationAssembly(launcherConfiguration);
        return applicationAssembly.createEngine();
    }

}
