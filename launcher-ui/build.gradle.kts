plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

dependencies {
    implementation(project(":launcher-common"))
}

application {
    mainClass.set("com.launcher.ui.LauncherApplication")
}

javafx {
    version = "23"

    modules = listOf(
        "javafx.controls",
        "javafx.fxml",
    )
}