package com.launcher.ui;

import com.launcher.app.bootstrap.Bootstrap;
import com.launcher.app.configuration.LauncherConfigurationResolver;
import com.launcher.app.configuration.ResolvedLauncherConfiguration;
import com.launcher.app.presentation.LaunchRequestResult;
import com.launcher.app.presentation.PresentationLaunchBoundary;
import com.launcher.ui.phase.JavaFxPresentationLaunchPhaseHandler;
import com.launcher.ui.result.JavaFxPresentationLaunchCompletionHandler;
import com.launcher.ui.startup.PresentationStartupResult;
import com.launcher.ui.startup.PresentationStartupState;
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

    private PresentationStartupResult startupResult;

    private PresentationLaunchBoundary presentationLaunchBoundary;

    @Override
    public void start(Stage primaryStage) {
        Label titleLabel = new Label(APPLICATION_NAME);
        Button launchButton = createButton("Launch", "launch-button");
        Button retryButton = createButton("Retry", "retry-configuration-button");
        Label launchStatusLabel = new Label();
        Label phaseStatusLabel = new Label();
        Label startupStatusLabel = new Label();

        initializePresentationLaunchBoundary(
                launchButton,
                retryButton,
                launchStatusLabel,
                phaseStatusLabel,
                startupStatusLabel
        );

        renderLaunchState(
                launchButton,
                retryButton,
                launchStatusLabel,
                startupStatusLabel
        );

        retryButton.setOnAction(event -> retryStartupConfiguration(
                launchButton,
                retryButton,
                launchStatusLabel,
                phaseStatusLabel,
                startupStatusLabel
        ));

        VBox content = new VBox(
                CONTENT_SPACING,
                titleLabel,
                launchStatusLabel,
                phaseStatusLabel,
                startupStatusLabel,
                retryButton,
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

    private void initializePresentationLaunchBoundary(
            Button launchButton,
            Button retryButton,
            Label launchStatusLabel,
            Label phaseStatusLabel,
            Label startupStatusLabel
    ) {
        String[] args = getParameters().getRaw().toArray(String[]::new);

        PresentationStartupInitializer initializer = new PresentationStartupInitializer();

        startupResult = initializer.initialize(
                () -> new LauncherConfigurationResolver().resolve(args),
                configuration ->
                        presentationLaunchBoundary = createPresentationLaunchBoundary(
                                configuration,
                                launchButton,
                                retryButton,
                                launchStatusLabel,
                                phaseStatusLabel,
                                startupStatusLabel
                        )
        );

        if (startupResult.state() == PresentationStartupState.AVAILABLE) {
            launchButton.setOnAction(event -> requestLaunch(
                    launchButton,
                    retryButton,
                    launchStatusLabel,
                    phaseStatusLabel,
                    startupStatusLabel
            ));
        }
    }

    private void retryStartupConfiguration(
            Button launchButton,
            Button retryButton,
            Label launchStatusLabel,
            Label phaseStatusLabel,
            Label startupStatusLabel
    ) {
        if (!startupResult.retryAvailable()) {
            return;
        }

        initializePresentationLaunchBoundary(
                launchButton,
                retryButton,
                launchStatusLabel,
                phaseStatusLabel,
                startupStatusLabel
        );

        renderLaunchState(
                launchButton,
                retryButton,
                launchStatusLabel,
                startupStatusLabel
        );
    }

    private PresentationLaunchBoundary createPresentationLaunchBoundary(
            ResolvedLauncherConfiguration configuration,
            Button launchButton,
            Button retryButton,
            Label launchStatusLabel,
            Label phaseStatusLabel,
            Label startupStatusLabel
    ) {
        JavaFxPresentationLaunchCompletionHandler resultHandler = new JavaFxPresentationLaunchCompletionHandler(
                result -> {
                    presentationLaunchStateMachine.onLaunchCompletion(result);
                    phaseStatusLabel.setText("");
                    renderLaunchState(
                            launchButton,
                            retryButton,
                            launchStatusLabel,
                            startupStatusLabel
                    );
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
            Button retryButton,
            Label launchStatusLabel,
            Label phaseStatusLabel,
            Label startupStatusLabel
    ) {
        LaunchRequestResult requestResult =
                presentationLaunchBoundary.requestLaunch();

        presentationLaunchStateMachine.onLaunchRequest(requestResult);

        if (requestResult == LaunchRequestResult.ACCEPTED) {
            phaseStatusLabel.setText("");
        }

        renderLaunchState(
                launchButton,
                retryButton,
                launchStatusLabel,
                startupStatusLabel
        );

    }

    private void renderLaunchState(
            Button launchButton,
            Button retryButton,
            Label launchStatusLabel,
            Label startupStatusLabel
    ) {

        PresentationLaunchState state = presentationLaunchStateMachine
                .currentState();

        PresentationStartupState startupState = startupResult.state();

        boolean isAvailable = startupState.allowsLaunch(
                presentationLaunchStateMachine.isLaunchAvailable()
        );

        boolean retryAvailable = startupResult.retryAvailable();

        launchButton.setDisable(!isAvailable);
        retryButton.setDisable(!retryAvailable);
        retryButton.setVisible(retryAvailable);
        retryButton.setManaged(retryAvailable);

        if (startupResult.state() == PresentationStartupState.AVAILABLE) {
            launchStatusLabel.setText(
                    PresentationLaunchStatusText.forState(
                            state,
                            presentationLaunchStateMachine.launchFailure()
                    )
            );
        }

        startupStatusLabel.setText(
                PresentationStartupStatusText.forResult(
                        startupResult
                )
        );
    }

    private Button createButton(String label, String id) {
        Button button = new Button(label);
        button.setId(id);
        return button;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
