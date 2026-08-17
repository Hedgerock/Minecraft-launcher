package com.launcher.core.launch;

import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.download.model.DownloadPlan;
import com.launcher.core.game.GameLaunchPlan;
import com.launcher.core.verification.model.VerificationPlan;
import com.launcher.model.manifest.Manifest;

public class LaunchContext {

    private Manifest manifest;
    private final LauncherConfiguration launcherConfiguration;
    private VerificationPlan verificationPlan;
    private DownloadPlan downloadPlan;
    private GameLaunchPlan gameLaunchPlan;

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

    public GameLaunchPlan getGameLaunchPlan() {
         return gameLaunchPlan;
    }

    public LaunchContext setGameLaunchPlan(GameLaunchPlan gameLaunchPlan) {
        this.gameLaunchPlan = gameLaunchPlan;

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
