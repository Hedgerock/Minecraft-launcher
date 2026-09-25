package com.launcher.ui.phase;

import com.launcher.app.presentation.phase.PresentationLaunchPhase;
import com.launcher.ui.UiThreadExecutor;
import com.launcher.ui.support.RecordingUiThreadExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaFxPresentationLaunchPhaseHandlerTest {
    private Consumer<PresentationLaunchPhase> consumer;
    private RecordingUiThreadExecutor uiThreadExecutor;

    @BeforeEach
    void setUp() {
        consumer = phase -> {};
        uiThreadExecutor = new RecordingUiThreadExecutor();
    }

    @Test
    void should_reject_null_presentation_launch_phase_consumer() {
        //when & then
        NullPointerException first = assertThrows(
                NullPointerException.class,
                () -> new JavaFxPresentationLaunchPhaseHandler(uiThreadExecutor, null)
        );

        NullPointerException second = assertThrows(
                NullPointerException.class,
                () -> new JavaFxPresentationLaunchPhaseHandler(null)
        );

        List.of(first, second).forEach(exception -> assertEquals(
                "presentationLaunchPhaseConsumer",
                exception.getMessage()
        ));
    }

    @Test
    void should_reject_null_ui_thread_executor() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new JavaFxPresentationLaunchPhaseHandler(null, consumer)
        );

        assertEquals(
                "uiThreadExecutor",
                exception.getMessage()
        );
    }

    @Test
    void should_reject_null_presentation_launch_phase() {
        //given
        JavaFxPresentationLaunchPhaseHandler handler =
                new JavaFxPresentationLaunchPhaseHandler(uiThreadExecutor, consumer);

        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> handler.handle(null)
        );

        assertEquals(
                "phase",
                exception.getMessage()
        );
    }

    @Test
    void should_schedule_phase_handling_on_ui_thread() {
        //given
        AtomicReference<Runnable> scheduled = new AtomicReference<>();
        AtomicReference<PresentationLaunchPhase> received = new AtomicReference<>();

        UiThreadExecutor executor = scheduled::set;

        JavaFxPresentationLaunchPhaseHandler handler =
                new JavaFxPresentationLaunchPhaseHandler(executor, received::set);

        //when
        handler.handle(PresentationLaunchPhase.LOADING_MANIFEST);

        //then
        assertNull(received.get());
        assertNotNull(scheduled.get());

        scheduled.get().run();

        assertEquals(PresentationLaunchPhase.LOADING_MANIFEST, received.get());
    }
}
