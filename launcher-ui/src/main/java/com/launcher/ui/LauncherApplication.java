package com.launcher.ui;

import com.launcher.app.bootstrap.Bootstrap;
import com.launcher.app.configuration.LauncherConfigurationResolver;
import com.launcher.app.presentation.LaunchRequestResult;
import com.launcher.app.presentation.PresentationLaunchBoundary;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.ui.phase.JavaFxPresentationLaunchPhaseHandler;
import com.launcher.ui.result.JavaFxPresentationLaunchCompletionHandler;
import com.launcher.ui.state.PresentationLaunchState;
import com.launcher.ui.state.PresentationLaunchStateMachine;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LauncherApplication extends Application {
    private static final String APPLICATION_NAME = "My little launcher";
    private static final int DEFAULT_WINDOW_WIDTH = 900;
    private static final int DEFAULT_WINDOW_HEIGHT = 550;
    private static final int CONTENT_SPACING = 16;

    private final PresentationLaunchStateMachine presentationLaunchStateMachine =
            new PresentationLaunchStateMachine();

    private PresentationLaunchBoundary presentationLaunchBoundary;

    @Override
    public void start(Stage primaryStage) {
        Label titleLabel = new Label(APPLICATION_NAME);
        Button launchButton = new Button("Launch");
        Label launchStatusLabel = new Label();
        Label phaseStatusLabel = new Label();

        presentationLaunchBoundary = createPresentationLaunchBoundary(launchButton, launchStatusLabel, phaseStatusLabel);

        launchButton.setOnAction(
                event -> requestLaunch(
                        launchButton,
                        launchStatusLabel,
                        phaseStatusLabel
                )
        );

        renderLaunchState(
                launchButton,
                launchStatusLabel
        );

        VBox content = new VBox(
                CONTENT_SPACING,
                titleLabel,
                launchStatusLabel,
                phaseStatusLabel,
                launchButton
        );

        content.setAlignment(Pos.CENTER);

        Scene scene = new Scene(
                content,
                DEFAULT_WINDOW_WIDTH,
                DEFAULT_WINDOW_HEIGHT
        );

        primaryStage.setTitle(APPLICATION_NAME);
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    @Override
    public void stop() {
        if (presentationLaunchBoundary != null) {
            presentationLaunchBoundary.close();
        }
    }

    private PresentationLaunchBoundary createPresentationLaunchBoundary(
            Button launchButton,
            Label launchStatusLabel,
            Label phaseStatusLabel
    ) {
        String[] args = getParameters()
                .getRaw()
                .toArray(String[]::new);

        LauncherConfiguration configuration = new LauncherConfigurationResolver()
                .resolve(args);

        JavaFxPresentationLaunchCompletionHandler resultHandler = new JavaFxPresentationLaunchCompletionHandler(
                result -> {
                    presentationLaunchStateMachine.onLaunchCompletion(result);
                    phaseStatusLabel.setText("");
                    renderLaunchState(launchButton, launchStatusLabel);
                }
        );

        JavaFxPresentationLaunchPhaseHandler phaseHandler = new JavaFxPresentationLaunchPhaseHandler(
                phase -> {
                    PresentationLaunchState state = presentationLaunchStateMachine.currentState();

                    if (state == PresentationLaunchState.LAUNCHING) {
                        phaseStatusLabel.setText(
                                PresentationLaunchPhaseText.forPhase(phase)
                        );
                    }
                }
        );

        Bootstrap bootstrap = new Bootstrap(configuration);

        return bootstrap.createPresentationLaunchBoundary(
                resultHandler,
                phaseHandler
        );
    }

    private void requestLaunch(
            Button launchButton,
            Label launchStatusLabel,
            Label phaseStatusLabel
    ) {
        LaunchRequestResult requestResult =
                presentationLaunchBoundary.requestLaunch();

        presentationLaunchStateMachine.onLaunchRequest(requestResult);

        if (requestResult == LaunchRequestResult.ACCEPTED) {
            phaseStatusLabel.setText("");
        }

        renderLaunchState(launchButton, launchStatusLabel);

    }

    private void renderLaunchState(
            Button launchButton,
            Label launchStatusLabel
    ) {

        PresentationLaunchState state = presentationLaunchStateMachine
                .currentState();

        launchButton.setDisable(
                !presentationLaunchStateMachine.isLaunchAvailable()
        );

        launchStatusLabel.setText(
                PresentationLaunchStatusText.forState(
                        state,
                        presentationLaunchStateMachine.launchFailure()
                )
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
