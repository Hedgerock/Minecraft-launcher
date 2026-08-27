package com.launcher.core.runtime;

import com.launcher.model.runtime.RuntimeEnvironment;

public interface RuntimeEnvironmentProvider {

    RuntimeEnvironment current();

}
