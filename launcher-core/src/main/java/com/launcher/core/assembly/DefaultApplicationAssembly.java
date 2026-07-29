package com.launcher.core.assembly;

import com.launcher.core.LauncherEngine;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.execution.SequentialExecutionStrategy;
import com.launcher.core.factory.infrastructure.DefaultLauncherInfrastructureFactory;
import com.launcher.core.factory.infrastructure.LauncherInfrastructureFactory;
import com.launcher.core.factory.service.DefaultLauncherServiceFactory;
import com.launcher.core.factory.service.LauncherServicesFactory;
import com.launcher.core.infrastructure.LauncherInfrastructure;
import com.launcher.core.operation.DefaultOperationManager;
import com.launcher.core.operation.OperationManager;
import com.launcher.core.operation.factory.DefaultOperationFactory;
import com.launcher.core.operation.factory.OperationFactory;
import com.launcher.core.service.LauncherServices;
import com.launcher.core.state.LauncherStateMachine;

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

        OperationFactory operationFactory = new DefaultOperationFactory(
                services.manifestService()
        );

        ExecutionStrategy executionStrategy = new SequentialExecutionStrategy();

        OperationManager operationManager = new DefaultOperationManager(
                operationFactory,
                executionStrategy
        );

        LauncherStateMachine stateMachine = new LauncherStateMachine(launcherInfrastructure.eventBus());

        return new LauncherEngine(stateMachine, operationManager);
    }
}
