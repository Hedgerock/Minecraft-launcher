package com.launcher.ui;

import com.launcher.app.presentation.phase.PresentationLaunchPhase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PresentationLaunchPhaseTextTest {

    @Test
    void should_return_starting_game_text_for_starting_game_phase() {
        //given
        PresentationLaunchPhase startingGamePhase = PresentationLaunchPhase.STARTING_GAME;

        //when
        String result = PresentationLaunchPhaseText.forPhase(startingGamePhase);

        //then
        assertEquals("Starting game...", result);
    }

    @Test
    void should_return_preparing_game_text_for_preparing_game_phase() {
        //given
        PresentationLaunchPhase preparingGamePhase = PresentationLaunchPhase.PREPARING_GAME;

        //when
        String result = PresentationLaunchPhaseText.forPhase(preparingGamePhase);

        //then
        assertEquals("Preparing game...", result);
    }

    @Test
    void should_return_downloading_text_for_downloading_phase() {
        //given
        PresentationLaunchPhase downloadingPhase = PresentationLaunchPhase.DOWNLOADING;

        //when
        String result = PresentationLaunchPhaseText.forPhase(downloadingPhase);

        //then
        assertEquals("Downloading...", result);
    }

    @Test
    void should_return_verifying_files_text_for_verifying_files_phase() {
        //given
        PresentationLaunchPhase verifyingFilesPhase = PresentationLaunchPhase.VERIFYING_FILES;

        //when
        String result = PresentationLaunchPhaseText.forPhase(verifyingFilesPhase);

        //then
        assertEquals("Verifying files...", result);
    }

    @Test
    void should_return_loading_manifest_text_for_loading_manifest_phase() {
        //given
        PresentationLaunchPhase loadingManifestPhase = PresentationLaunchPhase.LOADING_MANIFEST;

        //when
        String result = PresentationLaunchPhaseText.forPhase(loadingManifestPhase);

        //then
        assertEquals("Loading manifest...", result);
    }

    @Test
    void should_reject_null_presentation_launch_phase() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> PresentationLaunchPhaseText.forPhase(null)
        );

        assertEquals("phase", exception.getMessage());
    }
}
