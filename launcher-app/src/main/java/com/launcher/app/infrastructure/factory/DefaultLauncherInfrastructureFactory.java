package com.launcher.app.infrastructure.factory;

import com.launcher.api.http.HttpClient;
import com.launcher.api.http.JavaHttpClient;
import com.launcher.core.configuration.LauncherConfiguration;
import com.launcher.core.event.EventBus;
import com.launcher.core.factory.infrastructure.LauncherInfrastructureFactory;
import com.launcher.core.infrastructure.LauncherInfrastructure;
import com.launcher.core.storage.file.FileStorage;
import com.launcher.core.storage.file.LocalFileStorage;


public class DefaultLauncherInfrastructureFactory implements LauncherInfrastructureFactory {

    //TODO Нужна ли тут конфигурация?
    private final LauncherConfiguration configuration;

    public DefaultLauncherInfrastructureFactory(LauncherConfiguration configuration) {
        this.configuration = configuration;
    }

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
