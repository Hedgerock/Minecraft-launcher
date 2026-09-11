package com.launcher.core.game;

import com.launcher.core.game.builder.GameLaunchCommandBuilder;
import com.launcher.core.game.classpath.GameClasspath;
import com.launcher.core.game.classpath.builder.GameClasspathBuilder;
import com.launcher.core.game.classpath.formatter.ClasspathFormatter;
import com.launcher.core.resolve.model.LaunchVariables;
import com.launcher.core.runtime.JavaRuntimeSelector;
import com.launcher.core.runtime.compatibility.JavaRuntimeCompatibilityChecker;
import com.launcher.core.runtime.compatibility.model.JavaRuntimeCompatibilityRequest;
import com.launcher.core.runtime.javaexecutable.checker.JavaExecutableReadinessChecker;
import com.launcher.core.runtime.javaexecutable.resolver.JavaCommandPathResolver;
import com.launcher.core.runtime.model.JavaRuntimeSelectionRequest;
import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.model.manifest.LaunchInfo;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.RuntimeLibrarySelection;
import com.launcher.model.runtime.JavaExecutableReference;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class DefaultGameLaunchPlanBuilder implements GameLaunchPlanBuilder {
    private final DirectoryProvider directoryProvider;
    private final GameLaunchCommandBuilder launchCommandBuilder;
    private final GameClasspathBuilder gameClasspathBuilder;
    private final ClasspathFormatter classpathFormatter;
    private final JavaRuntimeSelector javaRuntimeSelector;
    private final JavaExecutableReadinessChecker javaExecutableReadinessChecker;
    private final JavaCommandPathResolver javaCommandPathResolver;
    private final JavaRuntimeCompatibilityChecker javaRuntimeCompatibilityChecker;

    public DefaultGameLaunchPlanBuilder(
            DirectoryProvider directoryProvider,
            GameLaunchCommandBuilder launchCommandBuilder,
            GameClasspathBuilder gameClasspathBuilder,
            ClasspathFormatter classpathFormatter,
            JavaRuntimeSelector javaRuntimeSelector,
            JavaExecutableReadinessChecker javaExecutableReadinessChecker,
            JavaCommandPathResolver javaCommandPathResolver,
            JavaRuntimeCompatibilityChecker javaRuntimeCompatibilityChecker
    ) {
        this.directoryProvider = directoryProvider;
        this.launchCommandBuilder = launchCommandBuilder;
        this.gameClasspathBuilder = gameClasspathBuilder;
        this.classpathFormatter = classpathFormatter;
        this.javaRuntimeSelector = javaRuntimeSelector;
        this.javaExecutableReadinessChecker = javaExecutableReadinessChecker;
        this.javaCommandPathResolver = javaCommandPathResolver;
        this.javaRuntimeCompatibilityChecker = javaRuntimeCompatibilityChecker;
    }

    public GameLaunchPlan build(
            Manifest manifest,
            RuntimeLibrarySelection runtimeLibrarySelection
    ) {
        return build(
                manifest,
                runtimeLibrarySelection,
                Optional.empty()
        );
    }

    @Override
    public GameLaunchPlan build(
            Manifest manifest,
            RuntimeLibrarySelection runtimeLibrarySelection,
            Optional<String> javaExecutableOverride
    ) {
        Objects.requireNonNull(manifest, "manifest");
        Objects.requireNonNull(runtimeLibrarySelection, "runtimeLibrarySelection");
        Objects.requireNonNull(javaExecutableOverride, "javaExecutableOverride");

        Path gameDirectory = directoryProvider.directories().game();

        GameClasspath gameClasspath = gameClasspathBuilder.build(
                manifest,
                runtimeLibrarySelection.libraries(),
                gameDirectory
        );

        Path nativesDirectory = directoryProvider.directories().natives();

        String classpath = classpathFormatter.format(gameClasspath);

        LaunchVariables launchVariables = new LaunchVariables(
                manifest.minecraftVersion(),
                gameDirectory,
                classpath,
                nativesDirectory
        );

        LaunchInfo launchInfo = manifest.launchInfo();

        JavaRuntimeSelectionRequest request = new JavaRuntimeSelectionRequest(
                launchInfo,
                javaExecutableOverride
        );

        JavaExecutableReference selectedJavaExecutableReference =
                javaRuntimeSelector.selectJavaExecutable(request);

        JavaExecutableReference resolvedJavaExecutableReference =
                javaCommandPathResolver.resolve(selectedJavaExecutableReference);

        javaExecutableReadinessChecker.checkReady(resolvedJavaExecutableReference);

        javaRuntimeCompatibilityChecker.checkCompatible(
                new JavaRuntimeCompatibilityRequest(
                        resolvedJavaExecutableReference,
                        launchInfo.javaVersionRequirement()
                )
        );

        List<String> command =
                launchCommandBuilder.build(
                        launchInfo,
                        launchVariables,
                        resolvedJavaExecutableReference
                );

        return new GameLaunchPlan(
                gameDirectory,
                command
        );
    }
}
