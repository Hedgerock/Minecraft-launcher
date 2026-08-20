package com.launcher.app.infrastructure.factory;

import com.launcher.api.http.LauncherHttpClient;
import com.launcher.api.http.JavaLauncherHttpClient;
import com.launcher.app.infrastructure.LauncherInfrastructure;
import com.launcher.core.event.EventBus;
import com.launcher.core.storage.file.FileStorage;
import com.launcher.core.storage.file.LocalFileStorage;


public class DefaultLauncherInfrastructureFactory implements LauncherInfrastructureFactory {

    @Override
    public LauncherInfrastructure createInfrastructure() {
        LauncherHttpClient launcherHttpClient = new JavaLauncherHttpClient();
        FileStorage fileStorage = new LocalFileStorage();
        EventBus eventBus = new EventBus();

        return new LauncherInfrastructure(
                launcherHttpClient,
                fileStorage,
                eventBus
        );
    }
}
