package com.launcher.core.launch;

import com.launcher.core.task.LauncherTask;

import java.util.List;

public class DefaultLaunchPlan implements LaunchPlan{

    private final List<LauncherTask> tasks;

    public DefaultLaunchPlan(List<LauncherTask> tasks) {
        this.tasks = List.copyOf(tasks);
    }

    @Override
    public List<LauncherTask> tasks() {
        return this.tasks;
    }
}
