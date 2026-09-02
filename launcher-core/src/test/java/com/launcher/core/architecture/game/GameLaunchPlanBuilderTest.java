package com.launcher.core.architecture.game;

import com.launcher.core.architecture.support.recording.RecordingClasspathFormatter;
import com.launcher.core.architecture.support.recording.RecordingDefaultGameLaunchCommandBuilder;
import com.launcher.core.architecture.support.recording.RecordingDirectoryProvider;
import com.launcher.core.architecture.support.recording.RecordingGameClasspathBuilder;
import com.launcher.core.architecture.support.recording.RecordingJavaExecutableReadinessChecker;
import com.launcher.core.architecture.support.recording.RecordingJavaRuntimeSelector;
import com.launcher.core.architecture.support.recording.RecordingManifestService;
import com.launcher.core.game.GameLaunchPlan;
import com.launcher.core.game.GameLaunchPlanBuilder;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.ManifestLoadResult;
import com.launcher.model.manifest.RuntimeLibrarySelection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameLaunchPlanBuilderTest {
    private RecordingManifestService manifestService;
    private RecordingDirectoryProvider directoryProvider;
    private RecordingDefaultGameLaunchCommandBuilder launchCommandBuilder;
    private RecordingGameClasspathBuilder recordingGameClasspathBuilder;
    private RecordingClasspathFormatter recordingClasspathFormatter;
    private RecordingJavaRuntimeSelector recordingJavaRuntimeSelector;
    private RecordingJavaExecutableReadinessChecker recordingJavaExecutableReadinessChecker;

    @BeforeEach
    void setUp() {
        manifestService = new RecordingManifestService();
        directoryProvider = new RecordingDirectoryProvider();
        launchCommandBuilder = new RecordingDefaultGameLaunchCommandBuilder();
        recordingGameClasspathBuilder = new RecordingGameClasspathBuilder();
        recordingClasspathFormatter = new RecordingClasspathFormatter();
        recordingJavaRuntimeSelector = new RecordingJavaRuntimeSelector();
        recordingJavaExecutableReadinessChecker = new RecordingJavaExecutableReadinessChecker();
    }

    @Test
    void should_fail_when_selected_java_executable_is_not_ready() {
        //given
        GameLaunchPlanBuilder gameLaunchPlanBuilder = new GameLaunchPlanBuilder(
                directoryProvider,
                launchCommandBuilder,
                recordingGameClasspathBuilder,
                recordingClasspathFormatter,
                recordingJavaRuntimeSelector,
                recordingJavaExecutableReadinessChecker
        );

        ManifestLoadResult manifestLoadResult = manifestService.loadManifest();
        Manifest manifest = manifestLoadResult.manifest();
        RuntimeLibrarySelection runtimeLibrarySelection = manifestLoadResult.runtimeLibrarySelection();

        recordingJavaExecutableReadinessChecker.setNotValid();

        //when & then
        assertThrows(
                IllegalStateException.class,
                () -> gameLaunchPlanBuilder.build(manifest, runtimeLibrarySelection)
        );

        assertNull(launchCommandBuilder.getLaunchInfo());
    }

    @Test
    void should_check_selected_java_executable_before_building_command() {
        //given
        GameLaunchPlanBuilder gameLaunchPlanBuilder = new GameLaunchPlanBuilder(
                directoryProvider,
                launchCommandBuilder,
                recordingGameClasspathBuilder,
                recordingClasspathFormatter,
                recordingJavaRuntimeSelector,
                recordingJavaExecutableReadinessChecker
        );

        ManifestLoadResult manifestLoadResult = manifestService.loadManifest();
        Manifest manifest = manifestLoadResult.manifest();
        RuntimeLibrarySelection runtimeLibrarySelection = manifestLoadResult.runtimeLibrarySelection();

        //when
        gameLaunchPlanBuilder.build(manifest, runtimeLibrarySelection);

        //then
        assertEquals(
                Path.of("new-java-executable"),
                recordingJavaExecutableReadinessChecker.getJavaExecutable()
        );
    }

    @Test
    void should_use_java_runtime_selector_for_java_executable() {
        //given
        GameLaunchPlanBuilder gameLaunchPlanBuilder = new GameLaunchPlanBuilder(
                directoryProvider,
                launchCommandBuilder,
                recordingGameClasspathBuilder,
                recordingClasspathFormatter,
                recordingJavaRuntimeSelector,
                recordingJavaExecutableReadinessChecker
        );

        ManifestLoadResult manifestLoadResult = manifestService.loadManifest();
        Manifest manifest = manifestLoadResult.manifest();
        RuntimeLibrarySelection runtimeLibrarySelection = manifestLoadResult.runtimeLibrarySelection();

        //when
        gameLaunchPlanBuilder.build(manifest, runtimeLibrarySelection);

        //then
        assertEquals(
                manifest.launchInfo(),
                recordingJavaRuntimeSelector.getLaunchInfo()
        );

        assertEquals(
                Path.of("new-java-executable"),
                launchCommandBuilder.getJavaExecutable()
        );
    }

    @Test
    void should_reject_null_runtime_library_selection() {
        //given
        GameLaunchPlanBuilder gameLaunchPlanBuilder = new GameLaunchPlanBuilder(
                directoryProvider,
                launchCommandBuilder,
                recordingGameClasspathBuilder,
                recordingClasspathFormatter,
                recordingJavaRuntimeSelector,
                recordingJavaExecutableReadinessChecker

        );

        ManifestLoadResult manifestLoadResult = manifestService.loadManifest();
        Manifest manifest = manifestLoadResult.manifest();

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> gameLaunchPlanBuilder.build(manifest, null)
        );

        assertTrue(exception.getMessage().contains("runtimeLibrarySelection"));
    }

    @Test
    void should_reject_null_manifest() {
        //given
        GameLaunchPlanBuilder gameLaunchPlanBuilder = new GameLaunchPlanBuilder(
                directoryProvider,
                launchCommandBuilder,
                recordingGameClasspathBuilder,
                recordingClasspathFormatter,
                recordingJavaRuntimeSelector,
                recordingJavaExecutableReadinessChecker
        );

        ManifestLoadResult manifestLoadResult = manifestService.loadManifest();
        RuntimeLibrarySelection runtimeLibrarySelection = manifestLoadResult.runtimeLibrarySelection();

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> gameLaunchPlanBuilder.build(null, runtimeLibrarySelection)
        );

        assertTrue(exception.getMessage().contains("manifest"));
    }

    @Test
    void should_build_game_launch_plan_with_game_directory_from_directory_provider() {
        //given
        GameLaunchPlanBuilder gameLaunchPlanBuilder = new GameLaunchPlanBuilder(
                directoryProvider,
                launchCommandBuilder,
                recordingGameClasspathBuilder,
                recordingClasspathFormatter,
                recordingJavaRuntimeSelector,
                recordingJavaExecutableReadinessChecker
        );

        ManifestLoadResult manifestLoadResult = manifestService.loadManifest();
        Manifest manifest = manifestLoadResult.manifest();
        RuntimeLibrarySelection runtimeLibrarySelection = manifestLoadResult.runtimeLibrarySelection();

        //when
        GameLaunchPlan gameLaunchPlan = gameLaunchPlanBuilder.build(manifest, runtimeLibrarySelection);

        //then
        assertEquals(
                directoryProvider.directories().game(),
                gameLaunchPlan.gameDirectory()
        );

        assertEquals(
                List.of("test-command"),
                gameLaunchPlan.command()
        );

        assertEquals(
                manifest.launchInfo(),
                launchCommandBuilder.getLaunchInfo()
        );

        assertEquals(
                manifest.minecraftVersion(),
                launchCommandBuilder.getLaunchVariables().versionName()
        );

        assertEquals(
                directoryProvider.directories().game(),
                launchCommandBuilder.getLaunchVariables().gameDirectory()
        );

        assertEquals(
                "path.to.not.BlankValue",
                launchCommandBuilder.getLaunchVariables().classpath()
        );

        assertEquals(
                manifest,
                recordingGameClasspathBuilder.getManifest()
        );

        assertEquals(
                runtimeLibrarySelection.libraries(),
                recordingGameClasspathBuilder.getLibraryEntries()
        );

        assertEquals(
                directoryProvider.directories().game(),
                recordingGameClasspathBuilder.getGameDirectory()
        );

        assertEquals(
                recordingGameClasspathBuilder.getGameClasspath(),
                recordingClasspathFormatter.getGameClasspath()
        );

        assertEquals(
                directoryProvider.directories().natives(),
                launchCommandBuilder.getLaunchVariables().nativesDirectory()
        );
    }

}
