package com.launcher.core.launch;

import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.download.DownloadPlan;
import com.launcher.core.verification.model.VerificationPlan;
import com.launcher.model.manifest.Manifest;

public class LaunchContext {

    private Manifest manifest;
    private final LauncherConfiguration launcherConfiguration;
    private VerificationPlan verificationPlan;
    private DownloadPlan downloadPlan;

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

    public VerificationPlan getVerificationPlan() {
        return verificationPlan;
    }

    public LaunchContext setVerificationPlan(VerificationPlan verificationPlan) {
        this.verificationPlan = verificationPlan;
        return this;
    }

    public DownloadPlan getDownloadPlan() {
        return downloadPlan;
    }

    public LaunchContext setDownloadPlan(DownloadPlan downloadPlan) {
        this.downloadPlan = downloadPlan;
        return this;
    }

    public LauncherConfiguration getLauncherConfiguration() {
        return launcherConfiguration;
    }
}
