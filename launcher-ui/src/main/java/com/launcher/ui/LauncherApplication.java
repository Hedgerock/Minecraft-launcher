package com.launcher.ui;

import com.launcher.ui.config.DefaultConfigValues;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class LauncherApplication extends Application {
    private static final String APPLICATION_LABEL_NAME = "My little launcher";

    @Override
    public void start(Stage primaryStage) throws Exception {
        final Label label = new Label(APPLICATION_LABEL_NAME);
        final Scene scene = new Scene(label, DefaultConfigValues.width, DefaultConfigValues.height);

        primaryStage.setTitle(APPLICATION_LABEL_NAME);
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
