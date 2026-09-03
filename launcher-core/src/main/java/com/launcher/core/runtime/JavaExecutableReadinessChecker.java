package com.launcher.core.runtime;

import com.launcher.model.runtime.JavaExecutableReference;

public interface JavaExecutableReadinessChecker {

    void checkReady(JavaExecutableReference javaExecutableReference);

}
