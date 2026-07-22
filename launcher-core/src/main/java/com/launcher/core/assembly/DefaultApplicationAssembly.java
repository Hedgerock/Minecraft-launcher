package com.launcher.core.assembly;

import com.launcher.core.LauncherEngine;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.factory.infrastructure.DefaultLauncherInfrastructureFactory;
import com.launcher.core.factory.infrastructure.LauncherInfrastructureFactory;
import com.launcher.core.factory.service.DefaultLauncherServiceFactory;
import com.launcher.core.factory.service.LauncherServicesFactory;
import com.launcher.core.factory.task.DefaultTaskFactory;
import com.launcher.core.factory.task.TaskFactory;
import com.launcher.core.infrastructure.LauncherInfrastructure;
import com.launcher.core.launch.DefaultLaunchPlan;
import com.launcher.core.launch.LaunchPlan;
import com.launcher.core.service.LauncherServices;
import com.launcher.core.state.LauncherStateMachine;
import com.launcher.core.task.TaskPipeline;

public class DefaultApplicationAssembly implements ApplicationAssembly {
    private final LauncherConfiguration launcherConfiguration;

    public DefaultApplicationAssembly(LauncherConfiguration launcherConfiguration) {
        this.launcherConfiguration = launcherConfiguration;
    }

    @Override
    public LauncherEngine createEngine() {

        LauncherInfrastructureFactory infrastructureFactory = new DefaultLauncherInfrastructureFactory(launcherConfiguration);

        LauncherInfrastructure launcherInfrastructure = infrastructureFactory.createInfrastructure();
        LauncherServicesFactory servicesFactory = new DefaultLauncherServiceFactory(
                launcherConfiguration,
                launcherInfrastructure
        );

        LauncherServices services = servicesFactory.createServices();

        LauncherStateMachine stateMachine = new LauncherStateMachine(launcherInfrastructure.eventBus());

        TaskFactory taskFactory = new DefaultTaskFactory(services);
        LaunchPlan launchPlan = new DefaultLaunchPlan(taskFactory.createTasks());

        TaskPipeline taskPipeline = new TaskPipeline(launchPlan);

        return new LauncherEngine(stateMachine, taskPipeline);
    }
}
