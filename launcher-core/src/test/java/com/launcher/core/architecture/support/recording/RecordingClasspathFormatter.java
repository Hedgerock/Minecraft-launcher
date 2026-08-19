package com.launcher.core.architecture.support.recording;

import com.launcher.core.game.classpath.GameClasspath;
import com.launcher.core.game.classpath.formatter.ClasspathFormatter;

public final class RecordingClasspathFormatter implements ClasspathFormatter {
    private GameClasspath gameClasspath;

    @Override
    public String format(GameClasspath classpath) {
        this.gameClasspath = classpath;

        return "path.to.not.BlankValue";
    }

    public GameClasspath getGameClasspath() {
        return gameClasspath;
    }
}
