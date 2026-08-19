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
import com.launcher.core.game.builder.DefaultGameLaunchCommandBuilder;
import com.launcher.core.game.GameLaunchPlanBuilder;
import com.launcher.core.game.builder.GameLaunchCommandBuilder;
import com.launcher.core.game.classpath.builder.DefaultGameClasspathBuilder;
import com.launcher.core.game.classpath.formatter.ClasspathFormatter;
import com.launcher.core.game.classpath.formatter.DefaultClasspathFormatter;
import com.launcher.core.operation.DefaultOperationManager;
import com.launcher.core.operation.OperationManager;
import com.launcher.core.operation.factory.DefaultOperationFactory;
import com.launcher.core.operation.factory.OperationFactory;
import com.launcher.core.resolve.DefaultLaunchArgumentResolver;
import com.launcher.core.resolve.LaunchArgumentResolver;
import com.launcher.core.state.LauncherStateMachine;

public class DefaultApplicationAssembly implements ApplicationAssembly {
    private final LauncherConfiguration launcherConfiguration;

    public DefaultApplicationAssembly(LauncherConfiguration launcherConfiguration) {
        this.launcherConfiguration = launcherConfiguration;
    }

    @Override
    public LauncherEngine createEngine() {

        LauncherInfrastructureFactory infrastructureFactory =
                new DefaultLauncherInfrastructureFactory();

        LauncherInfrastructure launcherInfrastructure = infrastructureFactory.createInfrastructure();
        OperationManager operationManager = createOperationManager(launcherInfrastructure);
        LauncherStateMachine stateMachine = new LauncherStateMachine(launcherInfrastructure.eventBus());

        return new LauncherEngine(stateMachine, operationManager);
    }

    private GameLaunchCommandBuilder getLaunchCommandBuilder() {
        LaunchArgumentResolver resolver = new DefaultLaunchArgumentResolver();

        return new DefaultGameLaunchCommandBuilder(resolver);
    }

    private OperationManager createOperationManager(LauncherInfrastructure launcherInfrastructure) {
        LauncherServicesFactory servicesFactory = new DefaultLauncherServiceFactory(
                launcherConfiguration,
                launcherInfrastructure
        );

        LauncherServices services = servicesFactory.createServices();
        DownloadPlanBuilder builder = new DownloadPlanBuilder();

        DefaultGameClasspathBuilder classpathBuilder = new DefaultGameClasspathBuilder();
        ClasspathFormatter classpathFormatter = new DefaultClasspathFormatter();
        GameLaunchCommandBuilder launchCommandBuilder = getLaunchCommandBuilder();

        GameLaunchPlanBuilder launchPlanBuilder = new GameLaunchPlanBuilder(
                services.directoryProvider(),
                launchCommandBuilder,
                classpathBuilder,
                classpathFormatter
        );

        OperationFactory operationFactory = new DefaultOperationFactory(
                services.manifestService(),
                services.verificationService(),
                services.downloadService(),
                services.directoryService(),
                services.gameService(),
                builder,
                launchPlanBuilder,
                launcherInfrastructure.eventBus()
        );

        ExecutionStrategy executionStrategy = new SequentialExecutionStrategy();

        return new DefaultOperationManager(
                operationFactory,
                executionStrategy
        );
    }
}
