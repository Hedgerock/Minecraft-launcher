package com.launcher.ui.failure;

import com.launcher.core.LaunchFailure;
import com.launcher.core.operation.failure.OperationFailure;
import com.launcher.core.operation.failure.OperationFailureCode;
import com.launcher.core.operation.type.OperationType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PresentationLaunchFailureMapperTest {
    private final PresentationLaunchFailureMapper mapper = new PresentationLaunchFailureMapper();

    @Test
    void should_return_generic_message_for_execution_failure() {
        //given
        PresentationLaunchFailure expected = new PresentationLaunchFailure("Launch failed");

        //when
        PresentationLaunchFailure result = mapper.mapExecutionFailure();

        //then
        assertEquals(expected, result);
    }

    @Test
    void should_not_expose_technical_details_in_presentation_failure() {
        //given
        String technicalPath =
                "C:\\Users\\Player\\launcher\\game\\client.jar";

        String technicalUrl =
                "https://cdn.example.text/private/client.jar";

        LaunchFailure failure = LaunchFailure.operation(
                OperationType.DOWNLOAD_FILES,
                new OperationFailure(
                        OperationFailureCode.UNKNOWN,
                        "Failed to write file: " + technicalPath,
                        Map.of(
                                "url",
                                technicalUrl
                        )
                )
        );

        //when
        PresentationLaunchFailure result = mapper.map(failure);

        //then
        assertEquals(
                getPresentationFailure("Could not download game files"),
                result
        );
    }

    @Test
    void should_return_general_message_for_lifecycle_failures() {
        //given
        LaunchFailure failure =
                LaunchFailure.lifecycle("Lifecycle message failed and through NullPointerException");


        //when
        PresentationLaunchFailure result = mapper.map(failure);

        //then
        PresentationLaunchFailure expected = getPresentationFailure("Launch failed");

        assertEquals(expected, result);
    }

    @Test
    void should_return_general_message_for_relative_launch_game_operation() {
        //given
        List<LaunchFailure> relativeOperations = List.of(
                getLaunchFailure(OperationType.BUILD_GAME_LAUNCH_PLAN),
                getLaunchFailure(OperationType.LAUNCH_GAME)
        );

        //when
        List<PresentationLaunchFailure> results = relativeOperations.stream().map(mapper::map).toList();

        //then
        PresentationLaunchFailure expected =
                getPresentationFailure("Could not start the game");

        results.forEach(result -> assertEquals(expected, result));
    }

    @Test
    void should_return_general_message_for_relative_prepare_game_operations() {
        //given
        List<LaunchFailure> relativeOperations = List.of(
                getLaunchFailure(OperationType.REPAIR),
                getLaunchFailure(OperationType.BUILD_DOWNLOAD_PLAN),
                getLaunchFailure(OperationType.PREPARE_DIRECTORIES),
                getLaunchFailure(OperationType.EXTRACT_NATIVES)
        );

        //when
        List<PresentationLaunchFailure> results = relativeOperations.stream().map(mapper::map).toList();

        //then
        PresentationLaunchFailure expected =
                getPresentationFailure("Could not prepare game files");

        results.forEach(result -> assertEquals(expected, result));
    }

    @Test
    void should_return_specific_message_for_download_files_operation() {
        //given
        LaunchFailure launchFailure = getLaunchFailure(OperationType.DOWNLOAD_FILES);

        //when
        PresentationLaunchFailure result = mapper.map(launchFailure);

        //then
        PresentationLaunchFailure expected =
                getPresentationFailure("Could not download game files");

        assertEquals(
                expected,
                result
        );
    }

    @Test
    void should_return_specific_message_for_verify_files_operation() {
        //given
        LaunchFailure launchFailure = getLaunchFailure(OperationType.VERIFY_FILES);

        //when
        PresentationLaunchFailure result = mapper.map(launchFailure);

        //then
        PresentationLaunchFailure expected =
                getPresentationFailure("Could not verify game files");

        assertEquals(
                expected,
                result
        );
    }

    @Test
    void should_return_specific_message_for_load_manifest_operation() {
        //given
        LaunchFailure launchFailure = getLaunchFailure(OperationType.LOAD_MANIFEST);

        //when
        PresentationLaunchFailure result = mapper.map(launchFailure);

        //then
        PresentationLaunchFailure expected =
                getPresentationFailure("Could not load game information");

        assertEquals(
                expected,
                result
        );
    }

    @Test
    void should_reject_null_launch_failure() {
        //when & then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> mapper.map(null)
        );

        assertEquals("failure", exception.getMessage());
    }

    private PresentationLaunchFailure getPresentationFailure(
            String message
    ) {
        return new PresentationLaunchFailure(message);
    }

    private LaunchFailure getLaunchFailure(
            OperationType operationType
    ) {
        return LaunchFailure.operation(
                operationType,
                new OperationFailure(
                        OperationFailureCode.UNKNOWN,
                        "Something went wrong",
                        Map.of()
                )
        );
    }
}
