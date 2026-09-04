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
import com.launcher.core.game.GameLaunchPlanBuilder;
import com.launcher.core.game.builder.DefaultGameLaunchCommandBuilder;
import com.launcher.core.game.builder.GameLaunchCommandBuilder;
import com.launcher.core.game.classpath.builder.DefaultGameClasspathBuilder;
import com.launcher.core.game.classpath.builder.GameClasspathBuilder;
import com.launcher.core.game.classpath.formatter.ClasspathFormatter;
import com.launcher.core.game.classpath.formatter.DefaultClasspathFormatter;
import com.launcher.core.natives.NativeExtractionPlanBuilder;
import com.launcher.core.operation.DefaultOperationManager;
import com.launcher.core.operation.OperationManager;
import com.launcher.core.operation.factory.DefaultOperationFactory;
import com.launcher.core.operation.factory.OperationFactory;
import com.launcher.core.resolve.DefaultLaunchArgumentResolver;
import com.launcher.core.resolve.LaunchArgumentResolver;
import com.launcher.core.resource.ResourcePathResolver;
import com.launcher.core.resource.SafeResourcePathResolver;
import com.launcher.core.runtime.JavaRuntimeSelector;
import com.launcher.core.runtime.ManifestJavaRuntimeSelector;
import com.launcher.core.runtime.RuntimeEnvironmentProvider;
import com.launcher.core.runtime.SystemRuntimeEnvironmentProvider;
import com.launcher.core.runtime.javaexecutable.checker.DefaultJavaExecutableReadinessChecker;
import com.launcher.core.runtime.javaexecutable.checker.JavaExecutableReadinessChecker;
import com.launcher.core.runtime.javaexecutable.resolver.DefaultJavaCommandPathResolver;
import com.launcher.core.runtime.javaexecutable.resolver.JavaCommandPathResolver;
import com.launcher.core.runtime.javaexecutable.resolver.ManifestJavaExecutableReferenceResolver;
import com.launcher.core.runtime.javaexecutable.resolver.provider.JavaCommandPathEnvironmentProvider;
import com.launcher.core.runtime.javaexecutable.resolver.provider.SystemJavaCommandPathEnvironmentProvider;
import com.launcher.core.state.LauncherStateMachine;
import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.core.storage.directory.LocalDirectoryProvider;

public final class DefaultApplicationAssembly implements ApplicationAssembly {
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

    private GameClasspathBuilder getClasspathBuilder(ResourcePathResolver resourcePathResolver) {
        return new DefaultGameClasspathBuilder(resourcePathResolver);
    }

    private GameLaunchPlanBuilder getLaunchPlanBuilder(
            ResourcePathResolver resourcePathResolver,
            DirectoryProvider directoryProvider
    ) {
        GameClasspathBuilder classpathBuilder = getClasspathBuilder(resourcePathResolver);
        ClasspathFormatter classpathFormatter = new DefaultClasspathFormatter();
        GameLaunchCommandBuilder launchCommandBuilder = getLaunchCommandBuilder();
        JavaRuntimeSelector javaRuntimeSelector = new ManifestJavaRuntimeSelector(
                new ManifestJavaExecutableReferenceResolver()
        );
        JavaExecutableReadinessChecker javaExecutableReadinessChecker = new DefaultJavaExecutableReadinessChecker();

        JavaCommandPathEnvironmentProvider javaCommandPathEnvironmentProvider =
                new SystemJavaCommandPathEnvironmentProvider();
        JavaCommandPathResolver javaCommandPathResolver = new DefaultJavaCommandPathResolver(
                javaCommandPathEnvironmentProvider.current()
        );

        return new GameLaunchPlanBuilder(
                directoryProvider,
                launchCommandBuilder,
                classpathBuilder,
                classpathFormatter,
                javaRuntimeSelector,
                javaExecutableReadinessChecker,
                javaCommandPathResolver
        );
    }

    private OperationManager createOperationManager(LauncherInfrastructure launcherInfrastructure) {
        ResourcePathResolver resourcePathResolver = new SafeResourcePathResolver();
        DirectoryProvider directoryProvider = new LocalDirectoryProvider(launcherConfiguration);
        RuntimeEnvironmentProvider environmentProvider = new SystemRuntimeEnvironmentProvider();

        LauncherServicesFactory servicesFactory = new DefaultLauncherServiceFactory(
                launcherConfiguration,
                launcherInfrastructure,
                resourcePathResolver,
                directoryProvider,
                environmentProvider
        );

        LauncherServices services = servicesFactory.createServices();
        DownloadPlanBuilder builder = new DownloadPlanBuilder();

        NativeExtractionPlanBuilder nativeExtractionPlanBuilder =
                new NativeExtractionPlanBuilder(directoryProvider);

        GameLaunchPlanBuilder launchPlanBuilder = getLaunchPlanBuilder(resourcePathResolver, directoryProvider);

        OperationFactory operationFactory = new DefaultOperationFactory(
                services.manifestService(),
                services.verificationService(),
                services.downloadService(),
                services.directoryService(),
                services.gameService(),
                services.nativeExtractionService(),
                builder,
                launchPlanBuilder,
                nativeExtractionPlanBuilder,
                launcherInfrastructure.eventBus()
        );

        ExecutionStrategy executionStrategy = new SequentialExecutionStrategy();

        return new DefaultOperationManager(
                operationFactory,
                executionStrategy
        );
    }
}
