package com.launcher.core.runtime.javaexecutable.checker;

import com.launcher.model.runtime.JavaExecutableReference;

public interface JavaExecutableReadinessChecker {

    void checkReady(JavaExecutableReference javaExecutableReference);

}
