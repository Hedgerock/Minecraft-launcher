package com.launcher.core.task;

import com.launcher.core.launch.LaunchContext;
import com.launcher.core.launch.LaunchPlan;
import com.launcher.core.result.Result;

public class TaskPipeline {

    private final LaunchPlan launchPlan;

    public TaskPipeline(LaunchPlan launchPlan) {
        this.launchPlan = launchPlan;
    }

    public Result execute(LaunchContext launchContext) {

        for (LauncherTask task: launchPlan.tasks()) {
            Result result = task.execute(launchContext);

            if (!result.success()) {
                return result;
            }

        }

        return TaskResult.success();
    }
}
