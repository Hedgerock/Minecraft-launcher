package com.launcher.app.bootstrap;

import com.launcher.core.LauncherEngine;
import com.launcher.core.configuration.LauncherConfiguration;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BootstrapTest {

    @Test
    void should_create_launcher_engine_through_bootstrap() {
        //given
        LauncherConfiguration configuration = new LauncherConfiguration(
                URI.create("https://localhost/manifest.json"),
                Path.of("")
        );

        Bootstrap bootstrap = new Bootstrap(configuration);

        //when
        LauncherEngine launcherEngine = bootstrap.createEngine();

        //then
        assertNotNull(launcherEngine);
    }

}
