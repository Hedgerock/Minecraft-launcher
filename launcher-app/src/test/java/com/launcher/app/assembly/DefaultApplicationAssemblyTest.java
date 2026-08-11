package com.launcher.app.assembly;

import com.launcher.core.LauncherEngine;
import com.launcher.core.configuration.LauncherConfiguration;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DefaultApplicationAssemblyTest {

    @Test
    void should_create_launcher_engine_with_default_application_assembly() {
        //given
        LauncherConfiguration configuration = new LauncherConfiguration(
                URI.create("https://localhost/manifest.json"),
                Path.of("")
        );

        ApplicationAssembly assembly = new DefaultApplicationAssembly(configuration);

        //when
        LauncherEngine launcherEngine = assembly.createEngine();

        //then
        assertNotNull(launcherEngine);
    }

}
