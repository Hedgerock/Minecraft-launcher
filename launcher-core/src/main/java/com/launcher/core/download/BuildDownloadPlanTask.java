package com.launcher.core.download;

import com.launcher.core.launch.LaunchContext;
import com.launcher.core.result.Result;
import com.launcher.core.state.LauncherState;
import com.launcher.core.task.LauncherTask;
import com.launcher.core.task.TaskResult;
import com.launcher.core.verification.model.VerificationPlan;

public class BuildDownloadPlanTask implements LauncherTask {
    private final DownloadPlanBuilder builder;

    public BuildDownloadPlanTask(DownloadPlanBuilder builder) {
        this.builder = builder;
    }

    @Override
    public LauncherState state() {
        return LauncherState.BUILDING_DOWNLOAD_PLAN;
    }

    @Override
    public Result execute(LaunchContext launchContext) {
        VerificationPlan verificationPlan = launchContext.getVerificationPlan();

        if (verificationPlan == null) {
            return TaskResult.failure("Verification plan not built");
        }

        DownloadPlan plan = builder.build(verificationPlan);
        launchContext.setDownloadPlan(plan);

        return TaskResult.success();
    }
}
