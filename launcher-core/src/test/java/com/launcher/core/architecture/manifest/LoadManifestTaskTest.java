package com.launcher.core.architecture.manifest;

import com.launcher.core.architecture.support.recording.RecordingManifestService;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.manifest.LoadManifestTask;
import com.launcher.core.manifest.ManifestService;
import com.launcher.core.result.Result;
import com.launcher.core.state.LauncherState;
import com.launcher.model.manifest.Manifest;
import com.launcher.model.manifest.ManifestLoadResult;
import com.launcher.model.manifest.RuntimeLibrarySelection;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadManifestTaskTest {

    @Test
    void should_store_manifest_and_runtime_library_selection() {
        //given
        ManifestLoadResult manifestLoadResult = new RecordingManifestService().loadManifest();
        Manifest manifest = manifestLoadResult.manifest();
        RuntimeLibrarySelection runtimeLibrarySelection = manifestLoadResult.runtimeLibrarySelection();

        ManifestService manifestService = () -> manifestLoadResult;
        LoadManifestTask task = new LoadManifestTask(manifestService);

        LaunchContext context = new LaunchContext(
                new LauncherConfiguration(
                        URI.create("currentPath"),
                        Path.of("")
                )
        );

        //when
        Result result = task.execute(context);

        //then
        assertTrue(result.success());
        assertSame(manifest, context.getManifest());
        assertSame(runtimeLibrarySelection, context.getRuntimeLibrarySelection());
    }

    @Test
    void should_return_loading_manifest_state() {
        //given
        ManifestLoadResult manifestLoadResult = new RecordingManifestService().loadManifest();
        LoadManifestTask task = new LoadManifestTask(() -> manifestLoadResult);

        //then
        assertEquals(
                LauncherState.LOADING_MANIFEST,
                task.state()
        );
    }

}
