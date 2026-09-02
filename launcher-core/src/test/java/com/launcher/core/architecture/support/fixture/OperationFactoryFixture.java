package com.launcher.core.architecture.support.fixture;

import com.launcher.core.architecture.support.recording.RecordVerificationService;
import com.launcher.core.architecture.support.recording.RecordingClasspathFormatter;
import com.launcher.core.architecture.support.recording.RecordingDirectoryProvider;
import com.launcher.core.architecture.support.recording.RecordingDirectoryService;
import com.launcher.core.architecture.support.recording.RecordingDownloadService;
import com.launcher.core.architecture.support.recording.RecordingGameService;
import com.launcher.core.architecture.support.recording.RecordingManifestService;
import com.launcher.core.architecture.support.recording.RecordingNativeExtractionService;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.download.DownloadPlanBuilder;
import com.launcher.core.event.EventBus;
import com.launcher.core.game.builder.DefaultGameLaunchCommandBuilder;
import com.launcher.core.game.GameLaunchPlanBuilder;
import com.launcher.core.game.classpath.builder.DefaultGameClasspathBuilder;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.manifest.ManifestService;
import com.launcher.core.natives.NativeExtractionPlanBuilder;
import com.launcher.core.operation.factory.DefaultOperationFactory;
import com.launcher.core.resolve.DefaultLaunchArgumentResolver;
import com.launcher.core.resource.SafeResourcePathResolver;
import com.launcher.core.runtime.ManifestJavaRuntimeSelector;
import com.launcher.core.runtime.NoOpJavaExecutableReadinessChecker;
import com.launcher.core.verification.model.VerificationPlan;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

public class OperationFactoryFixture {

    private final DefaultOperationFactory factory;

    public static DownloadPlanBuilder downloadPlanBuilder() {
        return new DownloadPlanBuilder();
    }

    public static GameLaunchPlanBuilder gameLaunchPlanBuilder() {
        return new GameLaunchPlanBuilder(
                new RecordingDirectoryProvider(),
                new DefaultGameLaunchCommandBuilder(
                        new DefaultLaunchArgumentResolver()
                ),
                new DefaultGameClasspathBuilder(
                        new SafeResourcePathResolver()
                ),
                new RecordingClasspathFormatter(),
                new ManifestJavaRuntimeSelector(),
                new NoOpJavaExecutableReadinessChecker()
        );
    }

    public static NativeExtractionPlanBuilder nativeExtractionPlanBuilder() {
        return new NativeExtractionPlanBuilder(new RecordingDirectoryProvider());
    }

    public OperationFactoryFixture() {
        ManifestService service = new RecordingManifestService();
        EventBus eventBus = new EventBus();
        this.factory = getDefaultOperationFactory(service, eventBus);

    }

    public static LaunchContext getContext() {
        return new LaunchContext(
                new LauncherConfiguration(
                        URI.create("currentPath"),
                        Path.of("")

                )
        );
    }

    public DefaultOperationFactory getFactory() {
        return factory;
    }

    private VerificationPlan getVerificationPlan() {
        return new VerificationPlan(
                List.of()
        );
    }

    private DefaultOperationFactory getDefaultOperationFactory(
            ManifestService manifestService,
            EventBus eventBus
    ) {
        return new DefaultOperationFactory(
                manifestService,
                new RecordVerificationService(getVerificationPlan()),
                new RecordingDownloadService(),
                new RecordingDirectoryService(),
                new RecordingGameService(),
                new RecordingNativeExtractionService(),
                downloadPlanBuilder(),
                gameLaunchPlanBuilder(),
                nativeExtractionPlanBuilder(),
                eventBus
        );
    }

}
