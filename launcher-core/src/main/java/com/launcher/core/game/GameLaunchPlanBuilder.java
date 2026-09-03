package com.launcher.core.game;

import com.launcher.core.game.builder.GameLaunchCommandBuilder;
import com.launcher.core.game.classpath.GameClasspath;
import com.launcher.core.game.classpath.builder.GameClasspathBuilder;
import com.launcher.core.game.classpath.formatter.ClasspathFormatter;
import com.launcher.core.resolve.model.LaunchVariables;
import com.launcher.core.runtime.javaexecutable.checker.JavaExecutableReadinessChecker;
import com.launcher.core.runtime.JavaRuntimeSelector;
import com.launcher.core.storage.directory.DirectoryProvider;
import com.launcher.model.manifest.LaunchInfo;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.RuntimeLibrarySelection;
import com.launcher.model.runtime.JavaExecutableReference;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class GameLaunchPlanBuilder {
    private final DirectoryProvider directoryProvider;
    private final GameLaunchCommandBuilder launchCommandBuilder;
    private final GameClasspathBuilder gameClasspathBuilder;
    private final ClasspathFormatter classpathFormatter;
    private final JavaRuntimeSelector javaRuntimeSelector;
    private final JavaExecutableReadinessChecker javaExecutableReadinessChecker;

    public GameLaunchPlanBuilder(
            DirectoryProvider directoryProvider,
            GameLaunchCommandBuilder launchCommandBuilder,
            GameClasspathBuilder gameClasspathBuilder,
            ClasspathFormatter classpathFormatter,
            JavaRuntimeSelector javaRuntimeSelector,
            JavaExecutableReadinessChecker javaExecutableReadinessChecker
    ) {
        this.directoryProvider = directoryProvider;
        this.launchCommandBuilder = launchCommandBuilder;
        this.gameClasspathBuilder = gameClasspathBuilder;
        this.classpathFormatter = classpathFormatter;
        this.javaRuntimeSelector = javaRuntimeSelector;
        this.javaExecutableReadinessChecker = javaExecutableReadinessChecker;
    }

    public GameLaunchPlan build(Manifest manifest, RuntimeLibrarySelection runtimeLibrarySelection) {
        Objects.requireNonNull(manifest, "manifest");
        Objects.requireNonNull(runtimeLibrarySelection, "runtimeLibrarySelection");

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

        JavaExecutableReference javaExecutableReference = javaRuntimeSelector.selectJavaExecutable(launchInfo);

        javaExecutableReadinessChecker.checkReady(javaExecutableReference);

        List<String> command =
                launchCommandBuilder.build(
                        launchInfo,
                        launchVariables,
                        javaExecutableReference
                );

        return new GameLaunchPlan(
                gameDirectory,
                command
        );
    }

}
