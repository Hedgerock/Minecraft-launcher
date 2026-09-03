package com.launcher.core.architecture.game;

import com.launcher.core.architecture.support.recording.RecordingClasspathFormatter;
import com.launcher.core.architecture.support.recording.RecordingDefaultGameLaunchCommandBuilder;
import com.launcher.core.architecture.support.recording.RecordingDirectoryProvider;
import com.launcher.core.architecture.support.recording.RecordingGameClasspathBuilder;
import com.launcher.core.architecture.support.recording.RecordingJavaCommandPathResolver;
import com.launcher.core.architecture.support.recording.RecordingJavaExecutableReadinessChecker;
import com.launcher.core.architecture.support.recording.RecordingJavaRuntimeSelector;
import com.launcher.core.architecture.support.recording.RecordingManifestService;
import com.launcher.core.game.GameLaunchPlan;
import com.launcher.core.game.GameLaunchPlanBuilder;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.ManifestLoadResult;
import com.launcher.model.manifest.RuntimeLibrarySelection;
import com.launcher.model.runtime.JavaExecutableReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
    private RecordingJavaCommandPathResolver recordingJavaCommandPathResolver;

    @BeforeEach
    void setUp() {
        manifestService = new RecordingManifestService();
        directoryProvider = new RecordingDirectoryProvider();
        launchCommandBuilder = new RecordingDefaultGameLaunchCommandBuilder();
        recordingGameClasspathBuilder = new RecordingGameClasspathBuilder();
        recordingClasspathFormatter = new RecordingClasspathFormatter();
        recordingJavaRuntimeSelector = new RecordingJavaRuntimeSelector();
        recordingJavaExecutableReadinessChecker = new RecordingJavaExecutableReadinessChecker();
        recordingJavaCommandPathResolver = new RecordingJavaCommandPathResolver();
    }

    @Test
    void should_use_resolved_java_executable_for_command_building() {
        //given
        GameLaunchPlanBuilder gameLaunchPlanBuilder = new GameLaunchPlanBuilder(
                directoryProvider,
                launchCommandBuilder,
                recordingGameClasspathBuilder,
                recordingClasspathFormatter,
                recordingJavaRuntimeSelector,
                recordingJavaExecutableReadinessChecker,
                recordingJavaCommandPathResolver
        );

        ManifestLoadResult manifestLoadResult = manifestService.loadManifest();
        Manifest manifest = manifestLoadResult.manifest();
        RuntimeLibrarySelection runtimeLibrarySelection = manifestLoadResult.runtimeLibrarySelection();

        //when
        gameLaunchPlanBuilder.build(manifest, runtimeLibrarySelection);

        //then
        assertSame(
                recordingJavaCommandPathResolver.getResolvedJavaExecutableReference(),
                launchCommandBuilder.getJavaExecutableReference()
        );
    }

    @Test
    void should_resolve_selected_java_executable_before_readiness_check() {
        //given
        GameLaunchPlanBuilder gameLaunchPlanBuilder = new GameLaunchPlanBuilder(
                directoryProvider,
                launchCommandBuilder,
                recordingGameClasspathBuilder,
                recordingClasspathFormatter,
                recordingJavaRuntimeSelector,
                recordingJavaExecutableReadinessChecker,
                recordingJavaCommandPathResolver
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

        assertNull(recordingJavaExecutableReadinessChecker.getJavaExecutableReference());
        assertNotNull(recordingJavaCommandPathResolver.getReceivedJavaExecutableReference());
    }

    @Test
    void should_fail_when_java_command_path_resolution_failed() {
        //given
        GameLaunchPlanBuilder gameLaunchPlanBuilder = new GameLaunchPlanBuilder(
                directoryProvider,
                launchCommandBuilder,
                recordingGameClasspathBuilder,
                recordingClasspathFormatter,
                recordingJavaRuntimeSelector,
                recordingJavaExecutableReadinessChecker,
                recordingJavaCommandPathResolver
        );

        ManifestLoadResult manifestLoadResult = manifestService.loadManifest();
        Manifest manifest = manifestLoadResult.manifest();
        RuntimeLibrarySelection runtimeLibrarySelection = manifestLoadResult.runtimeLibrarySelection();

        recordingJavaCommandPathResolver.setWithError();

        //when
        assertThrows(
                IllegalStateException.class,
                () -> gameLaunchPlanBuilder.build(manifest, runtimeLibrarySelection)
        );

        assertEquals(
                JavaExecutableReference.commandName("new-java-executable"),
                recordingJavaCommandPathResolver.getReceivedJavaExecutableReference()
        );

        assertNull(recordingJavaExecutableReadinessChecker.getJavaExecutableReference());
        assertNull(launchCommandBuilder.getLaunchInfo());
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
                recordingJavaExecutableReadinessChecker,
                recordingJavaCommandPathResolver
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
        assertNull(recordingJavaExecutableReadinessChecker.getJavaExecutableReference());
    }

    @Test
    void should_check_resolved_java_executable_before_building_command() {
        //given
        GameLaunchPlanBuilder gameLaunchPlanBuilder = new GameLaunchPlanBuilder(
                directoryProvider,
                launchCommandBuilder,
                recordingGameClasspathBuilder,
                recordingClasspathFormatter,
                recordingJavaRuntimeSelector,
                recordingJavaExecutableReadinessChecker,
                recordingJavaCommandPathResolver
        );

        ManifestLoadResult manifestLoadResult = manifestService.loadManifest();
        Manifest manifest = manifestLoadResult.manifest();
        RuntimeLibrarySelection runtimeLibrarySelection = manifestLoadResult.runtimeLibrarySelection();

        //when
        gameLaunchPlanBuilder.build(manifest, runtimeLibrarySelection);

        //then
        assertEquals(
                JavaExecutableReference.explicitPath("java"),
                recordingJavaExecutableReadinessChecker.getJavaExecutableReference()
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
                recordingJavaExecutableReadinessChecker,
                recordingJavaCommandPathResolver
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
                JavaExecutableReference.explicitPath("java"),
                launchCommandBuilder.getJavaExecutableReference()
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
                recordingJavaExecutableReadinessChecker,
                recordingJavaCommandPathResolver
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
                recordingJavaExecutableReadinessChecker,
                recordingJavaCommandPathResolver
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
                recordingJavaExecutableReadinessChecker,
                recordingJavaCommandPathResolver
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
