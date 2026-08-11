package com.launcher.app.assembly;

import com.launcher.app.infrastructure.LauncherInfrastructure;
import com.launcher.app.infrastructure.factory.DefaultLauncherInfrastructureFactory;
import com.launcher.app.infrastructure.factory.LauncherInfrastructureFactory;
import com.launcher.app.service.LauncherServices;
import com.launcher.app.service.factory.DefaultLauncherServiceFactory;
import com.launcher.app.service.factory.LauncherServicesFactory;
import com.launcher.core.LauncherEngine;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.download.DownloadPlanBuilder;
import com.launcher.core.execution.ExecutionStrategy;
import com.launcher.core.execution.SequentialExecutionStrategy;
import com.launcher.core.operation.DefaultOperationManager;
import com.launcher.core.operation.OperationManager;
import com.launcher.core.operation.factory.DefaultOperationFactory;
import com.launcher.core.operation.factory.OperationFactory;
import com.launcher.core.state.LauncherStateMachine;

public class DefaultApplicationAssembly implements ApplicationAssembly {
    private final LauncherConfiguration launcherConfiguration;

    public DefaultApplicationAssembly(LauncherConfiguration launcherConfiguration) {
        this.launcherConfiguration = launcherConfiguration;
    }

    @Override
    public LauncherEngine createEngine() {

        LauncherInfrastructureFactory infrastructureFactory =
                new DefaultLauncherInfrastructureFactory(launcherConfiguration);

        LauncherInfrastructure launcherInfrastructure = infrastructureFactory.createInfrastructure();
        OperationManager operationManager = createOperationManager(launcherInfrastructure);
        LauncherStateMachine stateMachine = new LauncherStateMachine(launcherInfrastructure.eventBus());

        return new LauncherEngine(stateMachine, operationManager);
    }

    private OperationManager createOperationManager(LauncherInfrastructure launcherInfrastructure) {
        LauncherServicesFactory servicesFactory = new DefaultLauncherServiceFactory(
                launcherConfiguration,
                launcherInfrastructure
        );

        LauncherServices services = servicesFactory.createServices();
        DownloadPlanBuilder builder = new DownloadPlanBuilder();

        OperationFactory operationFactory = new DefaultOperationFactory(
                services.manifestService(),
                services.verificationService(),
                services.downloadService(),
                services.directoryService(),
                builder,
                launcherInfrastructure.eventBus()
        );

        ExecutionStrategy executionStrategy = new SequentialExecutionStrategy();

        return new DefaultOperationManager(
                operationFactory,
                executionStrategy
        );
    }
}
