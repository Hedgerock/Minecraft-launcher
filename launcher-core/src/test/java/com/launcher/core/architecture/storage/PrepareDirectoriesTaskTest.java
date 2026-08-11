package com.launcher.core.architecture.storage;

import com.launcher.core.architecture.support.recording.RecordingDirectoryService;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.directory.PrepareDirectoriesTask;
import com.launcher.core.launch.LaunchContext;
import com.launcher.core.result.Result;
import com.launcher.core.result.SuccessResult;
import com.launcher.core.state.LauncherState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PrepareDirectoriesTaskTest {

    private RecordingDirectoryService directoryService;
    private PrepareDirectoriesTask task;
    private LaunchContext context;

    private LaunchContext getContext() {
        return new LaunchContext(
                new LauncherConfiguration(
                        URI.create("https://test.com"),
                        Path.of("")
                )
        );
    }

    @BeforeEach
    void setUp() {
        directoryService = new RecordingDirectoryService();
        task = new PrepareDirectoriesTask(directoryService);
        context = getContext();
    }

    @Test
    void should_prepare_directories() {
        //given

        //when
        task.execute(context);

        //then
        assertTrue(directoryService.isPrepareLauncherDirectoriesCalled());
    }

    @Test
    void should_return_success() {
        //given

        //when
        Result result = task.execute(context);

        //then
        assertInstanceOf(SuccessResult.class, result);
    }

    @Test
    void should_return_preparing_game_state() {

        //then
        assertEquals(
                LauncherState.PREPARING_GAME,
                task.state()
        );
    }
}
