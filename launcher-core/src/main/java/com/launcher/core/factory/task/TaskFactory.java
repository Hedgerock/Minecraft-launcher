package com.launcher.core.factory.task;

import com.launcher.core.task.LauncherTask;

import java.util.List;

public interface TaskFactory {

    List<LauncherTask> createTasks();

}
