package com.launcher.ui.result;

import com.launcher.app.presentation.completion.PresentationLaunchCompletion;
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

class JavaFxPresentationLaunchCompletionHandlerTest {
    private Consumer<PresentationLaunchCompletion> consumer;
    private RecordingUiThreadExecutor uiExecutor;

    @BeforeEach
    void setUp() {
        consumer = result -> {};
        uiExecutor = new RecordingUiThreadExecutor();
    }

    @Test
    void should_schedule_completion_handling_with_execution_failed() {
        //given
        AtomicReference<PresentationLaunchCompletion> receivedResult = new AtomicReference<>();
        Consumer<PresentationLaunchCompletion> consumer = receivedResult::set;

        JavaFxPresentationLaunchCompletionHandler handler =
                new JavaFxPresentationLaunchCompletionHandler(uiExecutor, consumer);

        PresentationLaunchCompletion expected = PresentationLaunchCompletion.executionFailure();

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
    void should_schedule_completion_handling_on_ui_thread() {
        //given
        AtomicReference<PresentationLaunchCompletion> receivedResult = new AtomicReference<>();
        Consumer<PresentationLaunchCompletion> consumer = receivedResult::set;

        JavaFxPresentationLaunchCompletionHandler handler =
                new JavaFxPresentationLaunchCompletionHandler(uiExecutor, consumer);

        PresentationLaunchCompletion expected = PresentationLaunchCompletion.fromLaunchResult(
                LaunchResult.success(LauncherState.RUNNING)
        );

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
    void should_reject_null_completion() {
        //given
        JavaFxPresentationLaunchCompletionHandler handler = new JavaFxPresentationLaunchCompletionHandler(consumer);

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> handler.handle(null)
        );

        assertEquals(
                "completion",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_completion_consumer() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new JavaFxPresentationLaunchCompletionHandler(null)
        );

        assertEquals(
                "presentationLaunchCompletionConsumer",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_ui_thread_executor() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new JavaFxPresentationLaunchCompletionHandler(null, consumer)
        );

        assertEquals(
                "uiThreadExecutor",
                exception.getMessage()
        );
    }
}
