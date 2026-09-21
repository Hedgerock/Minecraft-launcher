package com.launcher.ui.result;

import com.launcher.core.LaunchResult;
import com.launcher.core.state.LauncherState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaFxLauncherResultHandlerTest {
    private Consumer<LaunchResult> consumer;
    private RecordingUiThreadExecutor uiExecutor;

    @BeforeEach
    void setUp() {
        consumer = result -> {};
        uiExecutor = new RecordingUiThreadExecutor();
    }

    @Test
    void should_schedule_result_handling_on_ui_thread() {
        //given
        AtomicReference<LaunchResult> receivedResult = new AtomicReference<>();
        Consumer<LaunchResult> consumer = receivedResult::set;

        JavaFxLauncherResultHandler handler =
                new JavaFxLauncherResultHandler(uiExecutor, consumer);

        LaunchResult expected = LaunchResult.success(LauncherState.RUNNING);

        //when
        handler.handle(expected);

        //then
        assertNull(receivedResult.get());
        assertNotNull(uiExecutor.getAction());

        uiExecutor.getAction().run();

        assertEquals(
                expected,
                receivedResult.get()
        );
    }

    @Test
    void should_reject_null_result() {
        //given
        JavaFxLauncherResultHandler handler = new JavaFxLauncherResultHandler(consumer);

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

    @Test
    void should_reject_null_result_consumer() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new JavaFxLauncherResultHandler(null)
        );

        assertEquals(
                "resultConsumer",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_ui_thread_executor() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new JavaFxLauncherResultHandler(null, consumer)
        );

        assertEquals(
                "uiThreadExecutor",
                exception.getMessage()
        );
    }
}
