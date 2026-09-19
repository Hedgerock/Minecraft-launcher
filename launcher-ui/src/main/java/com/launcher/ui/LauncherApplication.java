package com.launcher.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class LauncherApplication extends Application {
    private static final String APPLICATION_NAME = "My little launcher";
    private static final int DEFAULT_WINDOW_WIDTH = 900;
    private static final int DEFAULT_WINDOW_HEIGHT = 550;

    @Override
    public void start(Stage primaryStage) {
        final Label label = new Label(APPLICATION_NAME);
        final Scene scene = new Scene(
                label,
                DEFAULT_WINDOW_WIDTH,
                DEFAULT_WINDOW_HEIGHT
        );

        primaryStage.setTitle(APPLICATION_NAME);
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
