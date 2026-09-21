package com.launcher.ui;

import com.launcher.app.bootstrap.Bootstrap;
import com.launcher.app.configuration.LauncherConfigurationResolver;
import com.launcher.app.presentation.LaunchRequestResult;
import com.launcher.app.presentation.PresentationLaunchBoundary;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.ui.result.JavaFxLauncherResultHandler;
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

    private PresentationLaunchBoundary presentationLaunchBoundary;

    @Override
    public void start(Stage primaryStage) {
        Label titleLabel = new Label(APPLICATION_NAME);
        Button launchButton = new Button("Launch");

        presentationLaunchBoundary = createPresentationLaunchBoundary(launchButton);

        launchButton.setOnAction(
                event -> requestLaunch(launchButton)
        );

        VBox content = new VBox(
                CONTENT_SPACING,
                titleLabel,
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
            Button launchButton
    ) {
        String[] args = getParameters()
                .getRaw()
                .toArray(String[]::new);

        LauncherConfiguration configuration = new LauncherConfigurationResolver()
                .resolve(args);

        JavaFxLauncherResultHandler resultHandler = new JavaFxLauncherResultHandler(
                result -> launchButton.setDisable(false)
        );

        Bootstrap bootstrap = new Bootstrap(configuration);

        return bootstrap.createPresentationLaunchBoundary(
                resultHandler
        );
    }

    private void requestLaunch(Button launchButton) {
        LaunchRequestResult requestResult =
                presentationLaunchBoundary.requestLaunch();

        if (requestResult == LaunchRequestResult.ACCEPTED) {
            launchButton.setDisable(true);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
