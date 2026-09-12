package com.launcher.core.architecture.operation;

import com.launcher.core.architecture.support.FailingJavaRuntimeCompatibilityChecker;
import com.launcher.core.architecture.support.fixture.OperationFactoryFixture;
import com.launcher.core.architecture.support.recording.RecordingDirectoryProvider;
import com.launcher.core.architecture.support.recording.RecordingManifestService;
import com.launcher.core.event.EventBus;
import com.launcher.core.execution.SequentialExecutionStrategy;
import com.launcher.core.game.DefaultGameLaunchPlanBuilder;
import com.launcher.core.game.builder.DefaultGameLaunchCommandBuilder;
import com.launcher.core.game.classpath.builder.DefaultGameClasspathBuilder;
import com.launcher.core.game.classpath.formatter.DefaultClasspathFormatter;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.operation.LaunchOperation;
import com.launcher.core.operation.impl.BuildGameLaunchPlanOperation;
import com.launcher.core.operation.result.OperationResult;
import com.launcher.core.resolve.DefaultLaunchArgumentResolver;
import com.launcher.core.resource.SafeResourcePathResolver;
import com.launcher.core.runtime.ManifestJavaRuntimeSelector;
import com.launcher.core.runtime.compatibility.JavaRuntimeCompatibilityChecker;
import com.launcher.core.runtime.compatibility.NoOpJavaRuntimeCompatibilityChecker;
import com.launcher.core.runtime.detection.NoOpJavaRuntimeVersionDetector;
import com.launcher.core.runtime.javaexecutable.checker.NoOpJavaExecutableReadinessChecker;
import com.launcher.core.runtime.javaexecutable.resolver.ManifestJavaExecutableReferenceResolver;
import com.launcher.core.runtime.javaexecutable.resolver.NoOpJavaCommandPathResolver;
import com.launcher.model.manifest.LoaderInfo;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.ManifestLoadResult;
import com.launcher.model.manifest.RuntimeLibrarySelection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildGameLaunchPlanOperationTest {
    private LaunchContext context;

    @BeforeEach
    void setUp() {
        context = OperationFactoryFixture.getContext();
    }

    @Test
    void should_return_failure_when_java_runtime_compatibility_check_failed() {
        //given
        int requiredJavaVersion = 17;
        RecordingManifestService manifestService = new RecordingManifestService();
        ManifestLoadResult loadResult = manifestService.loadManifest();
        Manifest manifest = loadResult.manifest();
        RuntimeLibrarySelection runtimeLibrarySelection = loadResult.runtimeLibrarySelection();

        context.setManifest(manifest);

        context.setRuntimeLibrarySelection(runtimeLibrarySelection);

        LaunchOperation operation = getLaunchOperation(new FailingJavaRuntimeCompatibilityChecker(requiredJavaVersion));

        //when
        OperationResult result = operation.execute();

        //then
        assertFalse(result.isSuccess());
        assertTrue(result.errorMessage().orElseThrow()
                .contains("Java runtime version 8 does not satisfy required Java version " + requiredJavaVersion));

        assertNull(context.getGameLaunchPlan());
    }

    @Test
    void should_return_failure_when_build_game_launch_plan_failed() {
        //given
        context.setManifest(
                new Manifest(
                        "1.12.2",
                        new LoaderInfo("fabric", "0.16.10"),
                        List.of(),
                        null,
                        List.of()
                )
        );

        LaunchOperation launchOperation = getLaunchOperation();

        //when
        OperationResult result = launchOperation.execute();

        //then
        assertFalse(result.isSuccess());
        assertTrue(result.errorMessage().orElseThrow().contains("Launch info not available"));
        assertNull(context.getGameLaunchPlan());

    }

    private LaunchOperation getLaunchOperation(JavaRuntimeCompatibilityChecker checker) {
        return new BuildGameLaunchPlanOperation(
                context,
                new SequentialExecutionStrategy(),
                new EventBus(),
                new DefaultGameLaunchPlanBuilder(
                        new RecordingDirectoryProvider(),
                        new DefaultGameLaunchCommandBuilder(
                                new DefaultLaunchArgumentResolver()
                        ),
                        new DefaultGameClasspathBuilder(
                                new SafeResourcePathResolver()
                        ),
                        new DefaultClasspathFormatter(),
                        new ManifestJavaRuntimeSelector(
                                new ManifestJavaExecutableReferenceResolver()
                        ),
                        new NoOpJavaExecutableReadinessChecker(),
                        new NoOpJavaCommandPathResolver(),
                        new NoOpJavaRuntimeVersionDetector(),
                        checker
                )
        );
    }

    private LaunchOperation getLaunchOperation() {
        return getLaunchOperation(new NoOpJavaRuntimeCompatibilityChecker());
    }
}
