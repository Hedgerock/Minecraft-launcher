package com.launcher.core;

import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.state.LauncherState;
import com.launcher.core.state.LauncherStateMachine;
import com.launcher.core.task.TaskPipeline;
import com.launcher.core.result.Result;

public class LauncherEngine {

    private final LauncherStateMachine stateMachine;
    private final TaskPipeline taskPipeline;

    public LauncherEngine(LauncherStateMachine stateMachine, TaskPipeline taskPipeline) {
        this.stateMachine = stateMachine;
        this.taskPipeline = taskPipeline;
    }

    public void launch(LauncherConfiguration configuration) {
        LaunchContext context = new LaunchContext(configuration);

        stateMachine.transition(LauncherState.CHECKING_UPDATES);

        Result result = taskPipeline.execute(context);

        if (!result.success()) {
            stateMachine.transition(LauncherState.FAILED);
            return;
        }

        stateMachine.transition(LauncherState.RUNNING);

    }
}
