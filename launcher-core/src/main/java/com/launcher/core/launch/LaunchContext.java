package com.launcher.core.launch;

import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.model.manifest.Manifest;

public class LaunchContext {

    private Manifest manifest;
    private final LauncherConfiguration launcherConfiguration;

    public LaunchContext(LauncherConfiguration launcherConfiguration) {
        this.launcherConfiguration = launcherConfiguration;
    }

    public Manifest getManifest() {
        return manifest;
    }

    public LaunchContext setManifest(Manifest manifest) {
        this.manifest = manifest;
        return this;
    }

    public LauncherConfiguration getLauncherConfiguration() {
        return launcherConfiguration;
    }
}
