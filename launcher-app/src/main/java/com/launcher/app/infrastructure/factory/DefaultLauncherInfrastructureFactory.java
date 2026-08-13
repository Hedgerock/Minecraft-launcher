package com.launcher.app.infrastructure.factory;

import com.launcher.api.http.HttpClient;
import com.launcher.api.http.JavaHttpClient;
import com.launcher.app.infrastructure.LauncherInfrastructure;
import com.launcher.core.event.EventBus;
import com.launcher.core.storage.file.FileStorage;
import com.launcher.core.storage.file.LocalFileStorage;


public class DefaultLauncherInfrastructureFactory implements LauncherInfrastructureFactory {

    @Override
    public LauncherInfrastructure createInfrastructure() {
        HttpClient httpClient = new JavaHttpClient();
        FileStorage fileStorage = new LocalFileStorage();
        EventBus eventBus = new EventBus();

        return new LauncherInfrastructure(
                httpClient,
                fileStorage,
                eventBus
        );
    }
}
