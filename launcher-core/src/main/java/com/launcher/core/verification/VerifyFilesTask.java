package com.launcher.core.verification;

import com.launcher.core.launch.LaunchContext;
import com.launcher.core.result.Result;
import com.launcher.core.state.LauncherState;
import com.launcher.core.task.LauncherTask;
import com.launcher.core.task.TaskResult;
import com.launcher.core.verification.model.VerificationPlan;
import com.launcher.model.manifest.Manifest;

public class VerifyFilesTask implements LauncherTask {

    private final VerificationService verificationService;

    public VerifyFilesTask(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @Override
    public LauncherState state() {
        return LauncherState.VERIFYING_FILES;
    }

    @Override
    public Result execute(LaunchContext launchContext) {
        Manifest manifest = launchContext.getManifest();

        if (manifest == null) {
            return TaskResult.failure("Manifest not loaded");
        }

        VerificationPlan plan = verificationService.verify(manifest);
        launchContext.setVerificationPlan(plan);

        return TaskResult.success();
    }
}
