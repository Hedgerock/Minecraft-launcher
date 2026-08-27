package com.launcher.model.runtime;

import java.util.Objects;

public record RuntimeEnvironment(
        OperatingSystem operatingSystem
) {

    public RuntimeEnvironment {
        Objects.requireNonNull(operatingSystem, "operatingSystem");
    }

}
