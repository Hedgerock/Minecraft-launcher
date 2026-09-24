package com.launcher.app.presentation.phase;

import com.launcher.core.state.LauncherState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PresentationLaunchPhaseMapperTest {
    private final PresentationLaunchPhaseMapper mapper = new PresentationLaunchPhaseMapper();

    @Test
    void should_return_optional_empty_for_unmapped_launcher_states() {
        //given
        List<LauncherState> unmappedStates = List.of(
                LauncherState.IDLE,
                LauncherState.CHECKING_UPDATES,
                LauncherState.BUILDING_DOWNLOAD_PLAN,
                LauncherState.RUNNING,
                LauncherState.FAILED
        );

        //when
        List<Optional<PresentationLaunchPhase>> result = unmappedStates.stream().map(mapper::map).toList();

        //then
        assertTrue(result.stream().allMatch(Optional::isEmpty));
    }

    @Test
    void should_return_optional_preparing_game_phase_for_preparing_game_launcher_states() {
        //given
        List<LauncherState> preparingGameStates = List.of(
                LauncherState.PREPARING_GAME,
                LauncherState.EXTRACTING_NATIVES,
                LauncherState.BUILDING_GAME_LAUNCH_PLAN
        );

        //when
        List<Optional<PresentationLaunchPhase>> result = preparingGameStates.stream().map(mapper::map).toList();

        //then
        assertTrue(
                result.stream()
                        .allMatch(
                                currentPhase ->
                                        currentPhase.isPresent()
                                                && currentPhase.get() == PresentationLaunchPhase.PREPARING_GAME
                        )
        );
    }

    @Test
    void should_return_optional_starting_game_phase_for_launching_launcher_state() {
        //given
        LauncherState state = LauncherState.LAUNCHING;

        //when
        Optional<PresentationLaunchPhase> result = mapper.map(state);

        //then
        assertTrue(result.isPresent());
        assertEquals(
                PresentationLaunchPhase.STARTING_GAME,
                result.get()
        );
    }

    @Test
    void should_return_optional_downloading_phase_for_downloading_launcher_state() {
        //given
        LauncherState state = LauncherState.DOWNLOADING;

        //when
        Optional<PresentationLaunchPhase> result = mapper.map(state);

        //then
        assertTrue(result.isPresent());
        assertEquals(
                PresentationLaunchPhase.DOWNLOADING,
                result.get()
        );
    }

    @Test
    void should_return_optional_verifying_files_phase_for_verifying_files_launcher_state() {
        //given
        LauncherState state = LauncherState.VERIFYING_FILES;

        //when
        Optional<PresentationLaunchPhase> result = mapper.map(state);

        //then
        assertTrue(result.isPresent());
        assertEquals(
                PresentationLaunchPhase.VERIFYING_FILES,
                result.get()
        );
    }

    @Test
    void should_return_optional_loading_manifest_phase_for_loading_manifest_launcher_state() {
        //given
        LauncherState state = LauncherState.LOADING_MANIFEST;

        //when
        Optional<PresentationLaunchPhase> result = mapper.map(state);

        //then
        assertTrue(result.isPresent());
        assertEquals(
                PresentationLaunchPhase.LOADING_MANIFEST,
                result.get()
        );
    }

    @Test
    void should_reject_null_launcher_state() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> mapper.map(null)
        );

        assertEquals("state", exception.getMessage());
    }
}
