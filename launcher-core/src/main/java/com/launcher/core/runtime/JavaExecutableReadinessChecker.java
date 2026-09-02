package com.launcher.core.runtime;

import java.nio.file.Path;

public interface JavaExecutableReadinessChecker {

    void checkReady(Path javaExecutable);

}
