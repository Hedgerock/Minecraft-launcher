package com.launcher.core.runtime.javaexecutable.resolver.provider;

import com.launcher.core.runtime.javaexecutable.resolver.model.JavaCommandPathEnvironment;

public interface JavaCommandPathEnvironmentProvider {

    JavaCommandPathEnvironment current();

}
