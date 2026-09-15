package com.launcher.app.result;

import com.launcher.core.LaunchResult;
import com.launcher.core.state.LauncherState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NoOpLauncherResultHandlerTest {
    private final NoOpLauncherResultHandler handler = new NoOpLauncherResultHandler();

    @Test
    void should_accept_launch_result() {
        //given
        LaunchResult result = LaunchResult.success(LauncherState.RUNNING);

        //when & then
        assertDoesNotThrow(() -> handler.handle(result));
    }

    @Test
    void should_reject_null_result() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> handler.handle(null)
        );

        assertEquals(
                "result",
                exception.getMessage()
        );
    }
}
